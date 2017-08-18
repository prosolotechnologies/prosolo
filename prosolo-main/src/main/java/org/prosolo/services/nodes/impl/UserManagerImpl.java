package org.prosolo.services.nodes.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.data.Result;
import org.prosolo.services.email.EmailSenderManager;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.UserCreationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.prosolo.services.upload.AvatarProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
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
	@Inject private AvatarProcessor avatarProcessor;
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;
	@Inject private UserGroupManager userGroupManager;

	@Autowired private PasswordEncrypter passwordEncrypter;
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private UserEntityESService userEntityESService;

	@Autowired private UserDataFactory userDataFactory;

	@Inject private EmailSenderManager emailSenderManager;

	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

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
	public User getUser(long organizationId, String email) throws DbConnectionException {
		try {
			email = email.toLowerCase();

			String query =
					"SELECT user " +
							"FROM User user " +
							"WHERE user.email = :email " +
							"AND user.verified = :verifiedEmail ";
			if (organizationId > 0) {
				query += "AND user.organization.id = :orgId";
			} else {
				query += "AND user.organization IS NULL ";
			}

			Query q = persistence.currentManager().createQuery(query)
					.setString("email", email)
					.setBoolean("verifiedEmail", true);

			if (organizationId > 0) {
				q.setLong("orgId", organizationId);
			}

			return (User) q.uniqueResult();
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error while retrieving user data");
		}
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
			String position, List<Long> roles, List<Long> rolesToUpdate, long creatorId) throws DbConnectionException, EventException {
		User user = resourceFactory.updateUser(userId, name, lastName, email, emailVerified,
				changePassword, password, position, roles, rolesToUpdate);
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

				User u = new User(oldCreatorId);
				//actor not passed
				result.addEvent(eventFactory.generateEventData(EventType.Delete, 0,
						u, null, null, null));
				//TODO check if line below is needed
				//userEntityESService.deleteNodeFromES(user);
				return result;
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Error", e);
				throw new DbConnectionException("Error while deleting competences, credentials and activities of user");
			}
	}

	@Override
	@Transactional
	public void setUserOrganization(long userId,long organizationId) {
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

	private Result<Void> assignNewOwner(long newCreatorId, long oldCreatorId) {
		Result<Void> result = new Result<>();
		result.addEvents(credentialManager.updateCredentialCreator(newCreatorId, oldCreatorId).getEvents());
		result.addEvents(competence1Manager.updateCompetenceCreator(newCreatorId, oldCreatorId).getEvents());
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

	@Override
	//nt
	public boolean createNewUserAndConnectToResources(long organizationId,
												   String name, String lastname, String emailAddress,
												   String password, String position, long unitId,
												   long unitRoleId, long userGroupId,
												   LearningContextData context, long actorId)
			throws DbConnectionException, EventException {
		Result<UserCreationData> res = self.createNewUserConnectToResourcesAndGetEvents(
				organizationId, name, lastname, emailAddress, password, position, unitId, unitRoleId,
				userGroupId, context, actorId);

		if (res.getResult() != null) {
			taskExecutor.execute(() -> {
				//TODO for now, we do not send emails
				//send email if new or activated account
//				if (res.getResult().isNewAccount()) {
//					boolean emailSent = false;
//					Session session = persistence.openSession();
//					Transaction t = null;
//					try {
//						t = session.beginTransaction();
//						emailSent = emailSenderManager.sendEmailAboutNewAccount(
//								res.getResult().getUser(), emailAddress, session);
//						t.commit();
//					} catch (Exception e) {
//						logger.error("Error", e);
//						e.printStackTrace();
//						if (t != null) {
//							t.rollback();
//						}
//					} finally {
//						session.close();
//					}
//					if (!emailSent) {
//						logger.error("Error while sending email to the user ("
//								+ res.getResult().getUser().getId() + ") with new account created");
//					}
//				}

				//generate events
				for (EventData ev : res.getEvents()) {
					try {
						eventFactory.generateEvent(ev);
						/*
						TODO this is a hack until we implement a mechanism for
						sequential handling of events
						 */
						if (ev.getEventType() == EventType.Registered ||
								ev.getEventType() == EventType.Account_Activated) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								logger.error("Error", e);
							}
						}
					} catch (EventException ee) {
						logger.error("Error", ee);
					}
				}
			});

			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public Result<UserCreationData> createNewUserConnectToResourcesAndGetEvents(long organizationId,
														   String name, String lastname, String emailAddress,
														   String password, String position, long unitId,
														   long unitRoleId, long userGroupId,
														   LearningContextData context, long actorId)
			throws DbConnectionException {
		try {
			Result<UserCreationData> res = new Result<>();

			Result<UserCreationData> newUserRes = createOrUpdateUser(
					organizationId, name, lastname, emailAddress, true,
					password, position, false, null, null,
					unitRoleId, context, actorId);

			res.setResult(newUserRes.getResult());
			res.addEvents(newUserRes.getEvents());

			//only if user is created/updated and retrieved successfully other operations can be performed
			if (newUserRes.getResult() != null) {
				if (unitId > 0 && unitRoleId > 0) {
					res.addEvents(unitManager.addUserToUnitWithRoleAndGetEvents(
							newUserRes.getResult().getUser().getId(), unitId, unitRoleId, actorId, context).getEvents());
				}

				if (userGroupId > 0) {
					res.addEvents(userGroupManager.addUserToTheGroupAndGetEvents(userGroupId, newUserRes.getResult().getUser().getId(),
							actorId, context).getEvents());
				}
			}

			return res;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw e;
		}
	}

	/*
	TODO merge this with resource factory createNewUser method when we agree on exact implementation
	and when there is time for refactoring
	 */
	private Result<User> createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
							   String password, String position, boolean system, InputStream avatarStream,
						       String avatarFilename, List<Long> roles, LearningContextData context)
			throws UserAlreadyRegisteredException, DbConnectionException {
		try {
			if (checkIfUserExists(emailAddress)) {
				throw new UserAlreadyRegisteredException("User with email address " + emailAddress + " is already registered.");
			}

			emailAddress = emailAddress.toLowerCase();

			User user = new User();
			user.setName(name);
			user.setLastname(lastname);

			user.setEmail(emailAddress);
			user.setVerified(emailVerified);
			user.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));

			if (organizationId > 0) {
				user.setOrganization((Organization) persistence.currentManager().load(Organization.class, organizationId));
			}

			if (password != null) {
				user.setPassword(passwordEncrypter.encodePassword(password));
				user.setPasswordLength(password.length());
			}

			user.setSystem(system);
			user.setPosition(position);

			user.setUserType(UserType.REGULAR_USER);

			if (roles != null) {
				for (Long id : roles) {
					Role role = (Role) persistence.currentManager().load(Role.class, id);
					user.addRole(role);
				}
			}
			user = saveEntity(user);

			try {
				if (avatarStream != null) {
					user.setAvatarUrl(avatarProcessor.storeUserAvatar(
							user.getId(), avatarStream, avatarFilename, true));
				}
			} catch (IOException e) {
				logger.error(e);
			}

			Result<User> res = new Result<>();
			res.setResult(user);
			/*
			TODO i don't know if we rely somewhere on event actor being new user that is just created
			so I used new user id as an actor in event, but this is probably not semantically correct
			 */
			res.addEvent(eventFactory.generateEventData(EventType.Registered, user.getId(),
					user, null, context, null));

			return res;
		} catch (UserAlreadyRegisteredException e) {
			logger.error("Error", e);
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while saving new user account");
		}
	}

	/**
	 * Creates or updates user and retrieves it with information if user account is just created.
	 *
	 * If user with {@code emailAddress} email exists, it is retrieved if it belongs to an organization
	 * with {@code organizationId} id, and role with {@code roleId} id is assigned to the user if he does not
	 * already have that role. Only role is updated for existing users. If such user (with email that belongs
	 * to the given organization) does not exist, result with null for user is returned.
	 *
	 * Deleted user is activated with name, last name and position updated and also role ({@code roleId}) added
	 *
	 * @param organizationId
	 * @param name
	 * @param lastname
	 * @param emailAddress
	 * @param emailVerified
	 * @param password
	 * @param position
	 * @param system
	 * @param avatarStream
	 * @param avatarFilename
	 * @param roleId
	 * @param context
	 * @param actorId
	 * @return
	 * @throws DbConnectionException
	 */
	private Result<UserCreationData> createOrUpdateUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
														String password, String position, boolean system, InputStream avatarStream,
														String avatarFilename, long roleId, LearningContextData context, long actorId)
			throws DbConnectionException {
		try {
			List<Long> roleIds = null;
			if (roleId > 0) {
				roleIds = new ArrayList<>();
				roleIds.add(roleId);
			}

			Result<User> newUserRes = createNewUser(organizationId, name, lastname, emailAddress, emailVerified,
					password, position, system, avatarStream, avatarFilename, roleIds, context);
			Result<UserCreationData> res = new Result<>();
			res.setResult(new UserCreationData(newUserRes.getResult(), true));
			res.addEvents(newUserRes.getEvents());
			return res;
		} catch (UserAlreadyRegisteredException e) {
			try {
				Result<UserCreationData> res = new Result<>();
				/*
				TODO for now we only consider user if he is a part of the passed organization already
				That should be revisited when there can be two users with same email address and different
				organization
				 */
				User user = getUser(organizationId, emailAddress.toLowerCase());
				if (user != null) {
					if (user.isDeleted()) {
						res.addEvents(activateUserAndUpdateBasicInfo(user, name, lastname, position, roleId,
								context, actorId).getEvents());
						res.setResult(new UserCreationData(user, true));
						return res;
					} else {
						res.setResult(new UserCreationData(user, false));
						if (roleId > 0) {
							boolean roleAlreadyAdded = false;
							for (Role role : user.getRoles()) {
								if (role.getId() == roleId) {
									roleAlreadyAdded = true;
									break;
								}
							}
							//add new role if user does not have it already
							if (!roleAlreadyAdded) {
								user.getRoles().add(
										(Role) persistence.currentManager().load(Role.class, roleId));
								User us = new User(user.getId());
								res.addEvent(eventFactory.generateEventData(
										EventType.USER_ROLES_UPDATED, actorId, us, null, context, null));
							}
						}
					}
				}

				return res;
			} catch (Exception ex) {
				logger.error("Error", ex);
				throw new DbConnectionException("Error while updating user data");
			}
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw e;
		}
	}

	/**
	 * Activates deleted user account and updates first name, last name, position and adds role if
	 * passed id is greater than 0
	 *
	 * @param user
	 * @param firstName
	 * @param lastName
	 * @param position
	 * @return
	 */
	private Result<Void> activateUserAndUpdateBasicInfo(User user, String firstName, String lastName,
														String position, long roleId,
														LearningContextData context, long actorId) {
		user.setDeleted(false);
		user.setName(firstName);
		user.setLastname(lastName);
		user.setPosition(position);

		/*
		TODO remove all old roles, this should be done when user is deleted.
		When that method is reimplemented, line below should be removed.
		 */
		user.getRoles().clear();
		//add role if passed
		if (roleId > 0) {
			user.getRoles().add((Role) persistence.currentManager()
					.load(Role.class, roleId));
		}

		User u = new User(user.getId());
		Result<Void> res = new Result<>();
		res.addEvent(eventFactory.generateEventData(EventType.Account_Activated, actorId,
				u, null, context, null));

		return res;
	}

}
