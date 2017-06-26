package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.*;

@Service("org.prosolo.services.nodes.UserManager")
public class UserManagerImpl extends AbstractManagerImpl implements UserManager {

	private static final long serialVersionUID = 7695010428900106309L;

	private static Logger logger = Logger.getLogger(UserManager.class);

	@Inject
	private Competence1Manager competence1Manager;
	@Inject
	private Activity1Manager activity1Manager;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private OrganizationManager organizationManager;
	@Inject
	private UserManager self;

	@Autowired private PasswordEncrypter passwordEncrypter;
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private UserEntityESService userEntityESService;

	@Autowired private UserDataFactory userDataFactory;

	@Override
	@Transactional (readOnly = true)
	public User getUser(String email) {
		email = email.toLowerCase();

		String query =
			"SELECT user " +
			"FROM User user " +
			"WHERE user.email = :email " +
				"AND user.verified = :verifiedEmail";

		User result = (User) persistence.currentManager().createQuery(query).
			setString("email", email).
		 	setBoolean("verifiedEmail",true).
			uniqueResult();
		if (result != null) {
			return result;
		}
		return null;
	}

	@Override
	@Transactional (readOnly = true)
	public boolean checkIfUserExists(String email) {
		email = email.toLowerCase();

		String query =
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.email = :email ";

		Long result = (Long) persistence.currentManager().createQuery(query).
				setString("email", email).
				uniqueResult();

		if (result != null && result > 0) {
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public Collection<User> getAllUsers() {
		String query =
			"SELECT user " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";

		@SuppressWarnings("unchecked")
		List<User> result = persistence.currentManager().createQuery(query).
				setBoolean("deleted", false).
				list();

		if (result != null) {
  			return result;
		}

		return new ArrayList<User>();
	}

	@Override
	@Transactional (readOnly = false)
	public User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream,
			String avatarFilename, List<Long> roles) throws UserAlreadyRegisteredException, EventException {
		return createNewUser(name, lastname, emailAddress, emailVerified, password, position,
				avatarStream, avatarFilename, roles, false);
	}

	@Override
	@Transactional (readOnly = false)
	public User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream,
			String avatarFilename, List<Long> roles, boolean isSystem) throws UserAlreadyRegisteredException, EventException {
		if (checkIfUserExists(emailAddress)) {
			throw new UserAlreadyRegisteredException("User with email address "+emailAddress+" is already registered.");
		}
		// it is called in a new transaction
		User newUser = resourceFactory.createNewUser(name, lastname, emailAddress, emailVerified, password, position, isSystem, avatarStream, avatarFilename, roles);

		eventFactory.generateEvent(EventType.Registered, newUser.getId());

		return newUser;
	}

	@Transactional (readOnly = false)
	public void addTopicPreferences(User user, Collection<Tag> tags) {
		if (user != null && tags != null) {
			user = merge(user);
			TopicPreference npPreference =  (TopicPreference) getUserPreferences(user,TopicPreference.class);

			for (Tag tag : tags) {
				npPreference.addPreferredKeyword(tag);
			}
			npPreference.setUser(user);
			saveEntity(npPreference);

			//user.addPreference(npPreference);
			saveEntity(user);
		}
	}

	@Override
	@Transactional (readOnly = true)
	public String getUserPosition(long id) throws DbConnectionException {
		try {
			String query =
					"SELECT user.position " +
							" FROM User user " +
							" WHERE user.id = :id ";

			String position = (String) persistence.currentManager().createQuery(query).
					setLong("id", id).
					uniqueResult();

			return position;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user position");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public  UserPreference getUserPreferences(User user, Class<? extends UserPreference> preferenceClass) {
		String query =
				"SELECT preference " +
				"FROM "+preferenceClass.getSimpleName()+" preference " +
				"LEFT JOIN preference.user user " +
				"WHERE user = :user ";
		List<UserPreference> preferences = persistence.currentManager().createQuery(query).
			setEntity("user", user).
			list();
		for (UserPreference preference : preferences) {
			if (preference.getClass().equals(preferenceClass))
				return   preference;
		}
		return null;
	}

	@Override
	@Transactional (readOnly = false)
	public String changePassword(long userId, String newPassword) throws ResourceCouldNotBeLoadedException {
//		User user = loadResource(User.class, userId);
//		user.setPassword(passwordEncrypter.encodePassword(newPassword));
//		user.setPasswordLength(newPassword.length());
//		return saveEntity(user);
		String newPassEncrypted = passwordEncrypter.encodePassword(newPassword);

		try {
			String query =
					"UPDATE User user " +
					"SET user.password = :newPassEncrypted, user.passwordLength = :newPassEncryptedLength " +
					"WHERE user.id = :userId ";

			persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setString("newPassEncrypted", newPassEncrypted)
				.setParameter("newPassEncryptedLength", newPassEncrypted.length())
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while updating user password");
		}
		return newPassEncrypted;
	}

	@Override
	@Transactional (readOnly = false)
	public String changePasswordWithResetKey(String resetKey, String newPassword) {
		String newPassEncrypted = passwordEncrypter.encodePassword(newPassword);

		try {
			String query =
					"UPDATE User user " +
					"SET user.password = :newPassEncrypted, user.passwordLength = :newPassEncryptedLength " +
					"WHERE user.id IN ( " +
						"SELECT resetKey.user.id " +
						"FROM ResetKey resetKey " +
						"WHERE resetKey.uid = :resetKey " +
					")";

			persistence.currentManager()
				.createQuery(query)
				.setString("resetKey", resetKey)
				.setString("newPassEncrypted", newPassEncrypted)
				.setParameter("newPassEncryptedLength", newPassEncrypted.length())
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while updating user password");
		}
		return newPassEncrypted;
	}
	@Override
	@Transactional (readOnly = false)
	public User changeAvatar(long userId, String newAvatarPath) throws ResourceCouldNotBeLoadedException {
		User user = loadResource(User.class, userId);
		user.setAvatarUrl(newAvatarPath);
		return saveEntity(user);
	}

	@Override
	@Transactional (readOnly = true)
	public List<User> loadUsers(List<Long> ids) {
		if (ids != null) {
			String query =
				"SELECT user " +
				"FROM User user " +
				"WHERE user.id IN (:userIds) ";

			@SuppressWarnings("unchecked")
			List<User> result = persistence.currentManager().createQuery(query).
				setParameterList("userIds", ids).
				list();

			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	@Transactional (readOnly = false)
	public User updateUser(long userId, String name, String lastName, String email,
			boolean emailVerified, boolean changePassword, String password,
			String position, List<Long> roles, long creatorId) throws DbConnectionException, EventException {
		User user = resourceFactory.updateUser(userId, name, lastName, email, emailVerified,
				changePassword, password, position, roles);
		eventFactory.generateEvent(EventType.Edit_Profile, creatorId, user);
		return user;
	}

	@Override
	@Transactional (readOnly = true)
	public List<User> getUsers(Long[] toExclude, int limit) {
		StringBuffer query = new StringBuffer();

		query.append(
			"SELECT user " +
			" FROM User user " +
			" WHERE user.deleted = :deleted "
		);

		if (toExclude != null && toExclude.length > 0) {
			query.append(
					"AND user.id NOT IN (:excludeIds) "
			);
		}

		Query q = persistence.currentManager().createQuery(query.toString()).
					setBoolean("deleted", false);

		if (toExclude != null && toExclude.length > 0) {
			q.setParameterList("excludeIds", toExclude);
		}

		@SuppressWarnings("unchecked")
		List<User> result = q
			.setMaxResults(limit)
			.list();

		if (result != null) {
  			return result;
		}

		return new ArrayList<User>();
	}

	@Override
	@Transactional (readOnly = true)
	public User getUserWithRoles(long id) throws DbConnectionException {
		try {
			String query =
				"SELECT user " +
				"FROM User user " +
				"LEFT JOIN fetch user.roles " +
				"WHERE user.id = :id ";

			User user = (User) persistence.currentManager().createQuery(query).
					setLong("id", id).
					uniqueResult();

			return user;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public String getUserEmail(long id) throws DbConnectionException {
		try {
			String query =
				"SELECT user.email " +
				"FROM User user " +
				"WHERE user.id = :id ";

			String email = (String) persistence.currentManager().createQuery(query).
					setLong("id", id).
					uniqueResult();

			return email;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user email");
		}
	}

	@Override
	public String getPassword(long userId) throws ResourceCouldNotBeLoadedException {
		try {
			String query =
				"SELECT user.password " +
				"FROM User user " +
				"WHERE user.id = :id ";

			String password = (String) persistence.currentManager().createQuery(query).
					setLong("id", userId).
					uniqueResult();

			return password;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user password");
		}
	}

	@Override
	//nt
	public void deleteUser(long oldCreatorId, long newCreatorId) throws DbConnectionException, EventException {
		Result<Void> result = self.deleteUserAndGetEvents(oldCreatorId, newCreatorId);
		for (EventData ev : result.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}

	@Override
	@Transactional
	public Result<Void> deleteUserAndGetEvents(long oldCreatorId, long newCreatorId) throws DbConnectionException {
			User user;
			Result<Void> result = new Result<>();
			try {
				user = loadResource(User.class, oldCreatorId);
				user.setDeleted(true);
				saveEntity(user);
				assignNewOwner(newCreatorId, oldCreatorId);
				saveEntity(user);
				result.addEvents(assignNewOwner(newCreatorId, oldCreatorId).getEvents());
				userEntityESService.deleteNodeFromES(user);
				return result;
			} catch (ResourceCouldNotBeLoadedException e) {
				throw new DbConnectionException("Error while deleting competences, credentials and activities of user");
			}
	}

	@Override
	public void setUserOrganization(long userId,long organizationId) {
		User user;
		try {
			user = loadResource(User.class,userId);
			user.setOrganization(organizationManager.getOrganizationById(organizationId));
			user = merge(user);
			saveEntity(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			e.printStackTrace();
		}
	}

	public PaginatedResult<UserData> getUsersWithRoles(int page, int limit, long filterByRoleId, List<Role> roles) {

		PaginatedResult<UserData> response = new PaginatedResult<>();

		String query =
				"SELECT DISTINCT user " +
				"FROM User user " +
				"LEFT JOIN FETCH user.roles role ";
				if(filterByRoleId > 0) {
					query+="WHERE role.id =: filterByRoleId AND user.deleted IS false ";
				}else{
					query+="WHERE role IN (:roles) AND user.deleted IS false ";
				}
				query+="ORDER BY user.name,user.lastname ASC ";

		Query result = persistence.currentManager().createQuery(query);

		if(filterByRoleId > 0){
			result.setLong("roleId",filterByRoleId);
		}else {
			result.setParameterList("roles",roles);
		}

		List<User> users = result
				.setFirstResult(page*limit)
				.setMaxResults(limit)
				.list();

		for(User u : users) {
			if(roles != null) {
				List<Role> adminRoles = new LinkedList<>(u.getRoles());

				UserData userData = new UserData(u,adminRoles);
				response.addFoundNode(userData);
			}
		}

		response.setAdditionalInfo(setFilter(roles,filterByRoleId));
		response.setHitsNumber(setUsersCountForFiltering(roles,filterByRoleId));

		return response;
	}

	private Long setUsersCountForFiltering(List<Role> roles,long filterByRoleId){
		String countQuery =
				"SELECT COUNT (DISTINCT user) " +
						"FROM User user " +
						"LEFT JOIN user.roles role ";
		if(filterByRoleId > 0) {
			countQuery += "WHERE role.id =: filterByRoleId AND user.deleted IS false";
		}else{
			countQuery += "WHERE role IN (:roles) AND user.deleted IS false";
		}


		Query result1 = persistence.currentManager().createQuery(countQuery);
		if(filterByRoleId > 0){
			result1.setLong("roleId",filterByRoleId);
		}else{
			result1.setParameterList("roles",roles);
		}

		return (Long)result1.uniqueResult();
	}

	private Map<String,Object> setFilter(List<Role> roles,long filterByRoleId){
		String countQuery =
				"SELECT COUNT (DISTINCT user) " +
						"FROM User user " +
						"LEFT JOIN user.roles role " +
						"WHERE role IN (:roles) AND user.deleted IS false ";

		long count = (long) persistence.currentManager().createQuery(countQuery)
				.setParameterList("roles",roles)
				.uniqueResult();

		List<RoleFilter> roleFilters = new ArrayList<>();
		RoleFilter defaultFilter = new RoleFilter(0, "All",count);
		roleFilters.add(defaultFilter);
		RoleFilter selectedFilter = defaultFilter;

		String query =
				"SELECT role, COUNT (DISTINCT user) " +
						"FROM User user "+
						"INNER JOIN user.roles role " +
						"WITH role in (:roles) AND user.deleted IS false " +
						"GROUP BY role";

		List<Object[]> result = persistence.currentManager().createQuery(query)
				.setParameterList("roles",roles)
				.list();

		for(Object[] objects : result){
			Role r = (Role) objects[0];
			long c = (long) objects[1];
			RoleFilter rf = new RoleFilter(r.getId(), r.getTitle(), c);
			roleFilters.add(rf);
			if(r.getId() == filterByRoleId) {
				selectedFilter = rf;
			}
		}

		Map<String, Object> additionalInfo = new HashMap<>();
		additionalInfo.put("filters", roleFilters);
		additionalInfo.put("selectedFilter", selectedFilter);

		return additionalInfo;
	}

	private Result<Void> assignNewOwner(long newCreatorId, long oldCreatorId) {
		Result<Void> result = new Result<>();
		result.addEvents(credentialManager.updateCredentialCreator(newCreatorId, oldCreatorId).getEvents());
		result.addEvents(competence1Manager.updateCompetenceCreator(newCreatorId, oldCreatorId).getEvents());
		activity1Manager.updateActivityCreator(newCreatorId, oldCreatorId);
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> getOrganizationUsers(long organizationId, boolean returnDeleted, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT user FROM User user " +
					 	   "WHERE user.organization.id = :orgId ";

			if (!returnDeleted) {
				query += "AND user.deleted = :boolFalse";
			}

			Query q = session
					.createQuery(query)
					.setLong("orgId", organizationId);

			if (!returnDeleted) {
				q.setBoolean("boolFalse", false);
			}

			return q.list();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving users");
		}
	}
}
