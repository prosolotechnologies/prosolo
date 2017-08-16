package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.UnitRoleMembership;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.event.context.data.UserContextData;
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
import org.prosolo.services.nodes.data.UnitRoleMembershipData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
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
	public User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream,
			String avatarFilename, List<Long> roles) throws UserAlreadyRegisteredException, EventException {
		return createNewUser(organizationId, name, lastname, emailAddress, emailVerified, password, position,
				avatarStream, avatarFilename, roles, false);
	}

	@Override
	@Transactional (readOnly = false)
	public User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream,
			String avatarFilename, List<Long> roles, boolean isSystem) throws UserAlreadyRegisteredException, EventException {
		if (checkIfUserExists(emailAddress)) {
			throw new UserAlreadyRegisteredException("User with email address "+emailAddress+" is already registered.");
		}
		// it is called in a new transaction
		User newUser = resourceFactory.createNewUser(
				organizationId,
				name,
				lastname,
				emailAddress,
				emailVerified,
				password,
				position,
				isSystem,
				avatarStream,
				avatarFilename,
				roles);

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
						   String position, List<Long> roles, List<Long> rolesToUpdate, UserContextData context)
			throws DbConnectionException, EventException {
		User user = resourceFactory.updateUser(userId, name, lastName, email, emailVerified,
				changePassword, password, position, roles, rolesToUpdate);

		String page = null;
		String lContext = null;
		String service = null;
		LearningContextData lcd = context.getContext();
		if (lcd != null) {
			page = lcd.getPage();
			lContext = lcd.getLearningContext();
			service = lcd.getService();
		}
		eventFactory.generateEvent(EventType.Edit_Profile, context.getActorId(), context.getOrganizationId(),
				context.getSessionId(), user, null, page, lContext, service, null, null);
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
	public UserData getUserWithRoles(long id, long organizationId) throws DbConnectionException {
		try {
			String query =
				"SELECT user " +
				"FROM User user " +
				"LEFT JOIN fetch user.roles " +
				"WHERE user.id = :id ";

			if (organizationId > 0) {
				query += "AND user.organization.id = :orgId";
			}

			Query q = persistence.currentManager().createQuery(query)
					.setLong("id", id);

			if (organizationId > 0) {
				q.setLong("orgId", organizationId);
			}

			User user = (User) q.uniqueResult();

			return new UserData(user, user.getRoles());
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
	public void deleteUser(long oldCreatorId, long newCreatorId, UserContextData context)
			throws DbConnectionException, EventException {
		Result<Void> result = self.deleteUserAndGetEvents(oldCreatorId, newCreatorId, context);
		for (EventData ev : result.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}

	@Override
	@Transactional
	public Result<Void> deleteUserAndGetEvents(long oldCreatorId, long newCreatorId, UserContextData context)
			throws DbConnectionException {
			User user;
			Result<Void> result = new Result<>();
			try {
				user = loadResource(User.class, oldCreatorId);
				user.setDeleted(true);
				saveEntity(user);
				result.addEvents(assignNewOwner(newCreatorId, oldCreatorId, context).getEvents());
				userEntityESService.deleteNodeFromES(user);
				return result;
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Error", e);
				throw new DbConnectionException("Error while deleting competences, credentials and activities of user");
			}
	}

	@Override
	@Transactional
	public void setUserOrganization(long userId, long organizationId) {
		try {
			User user = loadResource(User.class,userId);
			if(organizationId != 0) {
				user.setOrganization(loadResource(Organization.class, organizationId));
			}else{
				user.setOrganization(null);
			}
			saveEntity(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while setting organization for user");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<UserData> getUsersWithRoles(int page, int limit, long filterByRoleId, List<Role> roles,
													   long organizationId) throws  DbConnectionException {
		try {
			PaginatedResult<UserData> response = new PaginatedResult<>();

			String query =
					"SELECT DISTINCT user " +
					"FROM User user " +
					"LEFT JOIN FETCH user.roles role " +
					"WHERE user.deleted IS false ";

			if (organizationId > 0) {
				query += "AND user.organization.id = :orgId ";
			}
			if (filterByRoleId > 0) {
				query += "AND role.id = :filterByRoleId ";
			} else if (roles != null && !roles.isEmpty()) {
				query += "AND role IN (:roles) ";
			}
			query += "ORDER BY user.name,user.lastname ASC ";

			Query q = persistence.currentManager().createQuery(query);

			if (organizationId > 0) {
				q.setLong("orgId", organizationId);
			}

			if (filterByRoleId > 0) {
				q.setLong("filterByRoleId", filterByRoleId);
			} else if (roles != null && !roles.isEmpty()) {
				q.setParameterList("roles", roles);
			}

			List<User> users = q
					.setFirstResult(page * limit)
					.setMaxResults(limit)
					.list();

			for (User u : users) {
				List<Role> userRoles = new LinkedList<>(u.getRoles());
				UserData userData = new UserData(u, userRoles);
				response.addFoundNode(userData);
			}

			response.setAdditionalInfo(setFilter(organizationId, roles, filterByRoleId));
			response.setHitsNumber(setUsersCountForFiltering(organizationId, roles, filterByRoleId));

			return response;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while loading users");
		}
	}

	@Override
	@Transactional
	public void setOrganizationForUsers(List<UserData> users,Long organizationId) {
		for (UserData user : users) {
			setUserOrganization(user.getId(), organizationId);
		}
	}

	private Long setUsersCountForFiltering(long organizationId, List<Role> roles, long filterByRoleId){
		String countQuery =
				"SELECT COUNT (DISTINCT user) " +
				"FROM User user " +
				"LEFT JOIN user.roles role " +
				"WHERE user.deleted IS false ";

		if (organizationId > 0) {
			countQuery += "AND user.organization.id = :orgId ";
		}

		if (filterByRoleId > 0) {
			countQuery += "AND role.id = :filterByRoleId";
		} else if (roles != null && !roles.isEmpty()) {
			countQuery += "AND role IN (:roles)";
		}

		Query result1 = persistence.currentManager().createQuery(countQuery);

		if (organizationId > 0) {
			result1.setLong("orgId", organizationId);
		}

		if (filterByRoleId > 0){
			result1.setLong("filterByRoleId", filterByRoleId);
		} else if (roles != null && !roles.isEmpty()) {
			result1.setParameterList("roles", roles);
		}

		return (Long) result1.uniqueResult();
	}

	private Map<String,Object> setFilter(long organizationId, List<Role> roles, long filterByRoleId){
		String countQuery =
				"SELECT COUNT (DISTINCT user) " +
				"FROM User user " +
				"LEFT JOIN user.roles role " +
				"WHERE user.deleted IS false ";

		if (organizationId > 0) {
			countQuery += "AND user.organization.id = :orgId ";
		}

		if (roles != null && !roles.isEmpty()) {
			countQuery += "AND role IN (:roles) ";
		}

		Query q = persistence.currentManager().createQuery(countQuery);

		if (organizationId > 0) {
			q.setLong("orgId", organizationId);
		}

		if (roles != null && !roles.isEmpty()) {
			q.setParameterList("roles", roles);
		}

		long count = (long) q.uniqueResult();

		List<RoleFilter> roleFilters = new ArrayList<>();
		RoleFilter defaultFilter = new RoleFilter(0, "All", count);
		roleFilters.add(defaultFilter);
		RoleFilter selectedFilter = defaultFilter;

		String query =
				"SELECT role, COUNT (DISTINCT user) " +
				"FROM User user "+
				"INNER JOIN user.roles role ";

		if (roles != null && !roles.isEmpty()) {
			query += "WITH role in (:roles) ";
		}

		query += "WHERE user.deleted IS false ";

		if (organizationId > 0) {
			query += "AND user.organization.id = :orgId ";
		}
		query += "GROUP BY role";

		Query q1 = persistence.currentManager().createQuery(query);

		if (organizationId > 0) {
			q1.setLong("orgId", organizationId);
		}

		if (roles != null && !roles.isEmpty()) {
			q1.setParameterList("roles", roles);
		}

		List<Object[]> result = q1.list();

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

	private Result<Void> assignNewOwner(long newCreatorId, long oldCreatorId, UserContextData context) {
		Result<Void> result = new Result<>();
		result.addEvents(credentialManager.updateCredentialCreator(newCreatorId, oldCreatorId, context).getEvents());
		result.addEvents(competence1Manager.updateCompetenceCreator(newCreatorId, oldCreatorId, context).getEvents());
		activity1Manager.updateActivityCreator(newCreatorId, oldCreatorId);
		return result;
	}

	@Override
	@Transactional (readOnly = true)
	public UserData getUserData(long id) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager()
					.get(User.class, id);
			return new UserData(user);
		} catch(Exception e) {
			logger.error("Error", e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<UserData> getPaginatedOrganizationUsersWithRoleNotAddedToUnit(
			long orgId, long unitId, long roleId, int offset, int limit) throws DbConnectionException {
		try {
			PaginatedResult<UserData> res = new PaginatedResult<>();
			res.setHitsNumber(countOrganizationUsersWithRoleNotAddedToUnit(orgId, unitId, roleId));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getOrgUsersWithRoleNotAddedToUnit(orgId, unitId, roleId, offset, limit));
			}

			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving unit users");
		}
	}

	private List<UserData> getOrgUsersWithRoleNotAddedToUnit(
			long orgId, long unitId, long roleId, int offset, int limit) {
		String query =
				"SELECT user " +
				"FROM User user " +
				"INNER JOIN user.roles role " +
						"WITH role.id = :roleId " +
				"LEFT JOIN user.unitMemberships um " +
						"WITH um.unit.id = :unitId " +
						"AND um.role.id = :roleId " +
				"WHERE user.organization.id = :orgId " +
				"AND user.deleted IS FALSE " +
				"AND um IS NULL " +
				"ORDER BY user.lastname ASC, user.name ASC";

		List<User> result = persistence.currentManager()
				.createQuery(query)
				.setLong("unitId", unitId)
				.setLong("roleId", roleId)
				.setLong("orgId", orgId)
				.setMaxResults(limit)
				.setFirstResult(offset)
				.list();

		List<UserData> users = new ArrayList<>();
		for (User u : result) {
			users.add(new UserData(u));
		}

		return users;
	}

	private long countOrganizationUsersWithRoleNotAddedToUnit(long orgId, long unitId, long roleId) {
		String query =
				"SELECT COUNT(user) " +
				"FROM User user " +
				"INNER JOIN user.roles role " +
				"WITH role.id = :roleId " +
				"LEFT JOIN user.unitMemberships um " +
				"WITH um.unit.id = :unitId " +
				"AND um.role.id = :roleId " +
				"WHERE user.organization.id = :orgId " +
				"AND user.deleted IS FALSE " +
				"AND um IS NULL";
		return (long) persistence.currentManager()
				.createQuery(query)
				.setLong("unitId", unitId)
				.setLong("roleId", roleId)
				.setLong("orgId", orgId)
				.uniqueResult();
	}

}
