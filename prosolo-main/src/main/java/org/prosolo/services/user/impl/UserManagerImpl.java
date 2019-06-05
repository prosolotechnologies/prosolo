package org.prosolo.services.user.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.authentication.AuthenticatedUserService;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.user.data.UserAssessmentTokenData;
import org.prosolo.services.user.data.UserAssessmentTokenExtendedData;
import org.prosolo.services.user.data.UserCreationData;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service("org.prosolo.services.user.UserManager")
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
	private UserManager self;
	@Inject
	private AvatarProcessor avatarProcessor;
	@Inject
	private UnitManager unitManager;
	@Inject
	private UserGroupManager userGroupManager;
	@Inject
	private PasswordEncoder passwordEncoder;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private RoleManager roleManager;
	@Inject private PasswordResetManager passwordResetManager;
	@Inject private AuthenticatedUserService authenticatedUserService;

	@Override
	@Transactional (readOnly = true)
	public User getUser(String email) throws DbConnectionException {
		return getUser(email, false);
	}

	@Override
	@Transactional (readOnly = true)
	public User getUserIfNotDeleted(String email) throws DbConnectionException {
		return getUser(email, true);
	}

	private User getUser(String email, boolean onlyNotDeleted) {
		try {
			email = email.toLowerCase();

			String query =
					"SELECT user " +
					"FROM User user " +
					"WHERE user.email = :email " +
					"AND user.verified = :verifiedEmail ";
			if (onlyNotDeleted) {
				query += "AND user.deleted IS FALSE";
			}

			User result = (User) persistence.currentManager().createQuery(query).
					setString("email", email).
					setBoolean("verifiedEmail", true).
					uniqueResult();
			if (result != null) {
				return result;
			}
			return null;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving user data");
		}
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
			throw new DbConnectionException("Error retrieving user data");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public boolean checkIfUserExists(String email) throws DbConnectionException {
		return checkIfUserExists(email, false);
	}

	@Override
	@Transactional (readOnly = true)
	public boolean checkIfUserExistsAndNotDeleted(String email) throws DbConnectionException {
		return checkIfUserExists(email, true);
	}

	public boolean checkIfUserExists(String email, boolean excludeIfDeleted) throws DbConnectionException {
		try {
			email = email.toLowerCase();

			String query =
					"SELECT user.id " +
					"FROM User user " +
					"WHERE user.email = :email ";

			if (excludeIfDeleted) {
				query += "AND user.deleted IS FALSE";
			}

			Long result = (Long) persistence.currentManager().createQuery(query).
					setString("email", email).
					uniqueResult();

			if (result != null && result > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error checking if user account exists");
		}
	}

	@Override
	@Transactional
	public Collection<User> getAllUsers(long orgId) {
		return getAllUsers(orgId, persistence.currentManager());
	}

	@Override
	@Transactional
	public Collection<User> getAllUsers(long orgId, Session session) throws DbConnectionException {
		try {
			String query =
					"SELECT user " +
					"FROM User user " +
					"WHERE user.deleted = :deleted ";

			if (orgId > 0) {
				query += "AND user.organization.id = :orgId";
			}

			Query q = session.createQuery(query).
					setBoolean("deleted", false);

			if (orgId > 0) {
				q.setLong("orgId", orgId);
			}

			@SuppressWarnings("unchecked")
			List<User> result = q.list();

			if (result != null) {
				return result;
			}

			return new ArrayList<>();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving users");
		}
	}

	@Override
	//nt
	public User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
							  String password, String position, InputStream avatarStream,
							  String avatarFilename, List<Long> roles, boolean isSystem) throws DbConnectionException, IllegalDataStateException {
		Result<User> res = self.createNewUserAndGetEvents(
				organizationId,
				name,
				lastname,
				emailAddress,
				emailVerified,
				password,
				position,
				avatarStream,
				avatarFilename,
				roles,
				isSystem);

		eventFactory.generateAndPublishEvents(res.getEventQueue());

		return res.getResult();
	}

	@Override
	public User createNewUserAndSendEmail(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
										  String password, String position, InputStream avatarStream,
										  String avatarFilename, List<Long> roles, boolean isSystem) throws IllegalDataStateException {
		Result<User> res = self.createNewUserSendEmailAndGetEvents(
				organizationId,
				name,
				lastname,
				emailAddress,
				emailVerified,
				password,
				position,
				avatarStream,
				avatarFilename,
				roles,
				isSystem);

		eventFactory.generateAndPublishEvents(res.getEventQueue());

		return res.getResult();
	}

	@Override
	@Transactional
	public Result<User> createNewUserSendEmailAndGetEvents(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
												   String password, String position, InputStream avatarStream,
												   String avatarFilename, List<Long> roles, boolean isSystem) throws IllegalDataStateException {
		Result<User> res = self.createNewUserAndGetEvents(
				organizationId,
				name,
				lastname,
				emailAddress,
				emailVerified,
				password,
				position,
				avatarStream,
				avatarFilename,
				roles,
				isSystem);

		if (CommonSettings.getInstance().config.emailNotifier.activated) {
			//send email to new user for password recovery
			try {
				sendNewPassword(res.getResult());
			} catch (Exception e) {
				logger.error("error sending the password reset email", e);
				//don't throw exception since we don't want to rollback the transaction just because email could not be sent.
			}
		}
		return res;
	}

	private void sendNewPassword(User user) {
		boolean resetLinkSent = passwordResetManager.initiatePasswordReset(user, user.getEmail(),
				CommonSettings.getInstance().config.appConfig.domain + "recovery", persistence.currentManager());
		if (resetLinkSent) {
			logger.info("Password instructions have been sent");
		} else {
			logger.error("Error sending password instruction");
		}
	}


	@Override
	@Transactional (readOnly = false)
	public Result<User> createNewUserAndGetEvents(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream,
			String avatarFilename, List<Long> roles, boolean isSystem) throws DbConnectionException {

		Result<User> result = new Result<>();

		try {

			if (checkIfUserExists(emailAddress)) {
				User user = getUser(emailAddress);

				// if user was deleted, revoke his account
				if (user.isDeleted()) {
					user.setDeleted(false);
					saveEntity(user);
				}
				result.setResult(user);

				return result;
			}

			emailAddress = emailAddress.toLowerCase();

			User user = new User();
			user.setName(name);
			user.setLastname(lastname);

			user.setEmail(emailAddress);
			user.setVerified(emailVerified);
			user.setVerificationKey(UUID.randomUUID().toString().replace("-", ""));

			if (organizationId > 0) {
				Organization org = (Organization) persistence.currentManager().load(Organization.class, organizationId);
				user.setOrganization(org);
				//setting initial number of tokens no matter if tokens are enabled at the moment
				user.setNumberOfTokens(org.getInitialNumberOfTokensGiven());
			}

			if (password != null) {
				user.setPassword(passwordEncoder.encode(password));
				user.setPasswordLength(password.length());
			}

			user.setSystem(isSystem);
			user.setPosition(position);

			user.setUserType(UserType.REGULAR_USER);

			if(roles == null) {
				user.addRole(roleManager.getRoleByName(SystemRoleNames.USER));
			} else {
				for(Long id : roles) {
					Role role = (Role) persistence.currentManager().load(Role.class, id);
					user.addRole(role);
				}
			}
			user = saveEntity(user);

			try {
				if (avatarStream != null) {
					user.setAvatarUrl(avatarProcessor.storeUserAvatar(user.getId(), avatarStream, avatarFilename, true));
					user = saveEntity(user);
				}
			} catch (IOException e) {
				logger.error(e);
			}

			result.appendEvent(eventFactory.generateEventData(
					EventType.Registered, UserContextData.ofActor(user.getId()),null, null, null, null));

			result.setResult(user);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving new user account");
		}
		return result;
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
		String newPassEncrypted = passwordEncoder.encode(newPassword);

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
			flush();
			//refresh user session data
			authenticatedUserService.refreshUserSessionData();
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error updating user password");
		}
		return newPassEncrypted;
	}

	@Override
	@Transactional (readOnly = false)
	public String changePasswordWithResetKey(String resetKey, String newPassword) {
		String newPassEncrypted = passwordEncoder.encode(newPassword);

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
			throw new DbConnectionException("Error updating user password");
		}
		return newPassEncrypted;
	}
	@Override
	@Transactional (readOnly = false)
	public void changeAvatar(long userId, String newAvatarPath) throws ResourceCouldNotBeLoadedException {
		try {
			User user = loadResource(User.class, userId);
			user.setAvatarUrl(newAvatarPath);
			flush();
			//refresh user session data
			authenticatedUserService.refreshUserSessionData();
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error updating the user avatar for the user: " + userId);
		}
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
	// nt
	public User updateUser(long userId, String name, String lastName, String email,
						   boolean emailVerified, boolean changePassword, String password,
						   String position, int numberOfTokens, List<Long> newRoleList, List<Long> allRoles, UserContextData context)
			throws DbConnectionException {
		Result<User> result = self.updateUserAndGetEvents(userId, name, lastName, email, emailVerified,
				changePassword, password, position, numberOfTokens, newRoleList, allRoles, context);

		eventFactory.generateAndPublishEvents(result.getEventQueue());

		return result.getResult();
	}

	@Override
	@Transactional (readOnly = false)
	public Result<User> updateUserAndGetEvents(long userId, String name, String lastName, String email,
											   boolean emailVerified, boolean changePassword, String password,
											   String position, int numberOfTokens, List<Long> newRoleList,
											   List<Long> allRoles, UserContextData context) throws DbConnectionException {
		Result<User> result = new Result<>();
		try {
			User user = loadResource(User.class, userId);
			user.setName(name);
			user.setLastname(lastName);
			user.setEmail(email);
			user.setPosition(position);
			user.setNumberOfTokens(numberOfTokens);
			user.setVerified(true);

			if (changePassword) {
				user.setPassword(passwordEncoder.encode(password));
				user.setPasswordLength(password.length());
			}

			// remove the following roles (if user has them)
			Set<Long> removedRoles = new HashSet<>(allRoles);
			removedRoles.removeAll(newRoleList);

			for (Long roleId : removedRoles) {
				boolean removed = user.removeRoleById(roleId);

				if (removed) {
					// delete all unit memberships in roles that are removed
					result.appendEvents(unitManager.removeUserFromAllUnitsWithRoleAndGetEvents(userId, roleId, context).getEventQueue());
				}
			}

			// add roles that user did not have previously
			for (Long roleId : newRoleList) {
				if (!user.hasRole(roleId)) {
					Role role = (Role) persistence.currentManager().load(Role.class, roleId);
					user.addRole(role);
				}
			}

			result.setResult(user);

			result.appendEvent(eventFactory.generateEventData(EventType.Edit_Profile, context, user, null, null, null));

			return result;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error updating user data");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public List<User> getUsers(Long[] toExclude, int limit) {
		StringBuffer query = new StringBuffer();

		query.append(
			"SELECT user " +
			"FROM User user " +
			"WHERE user.deleted = :deleted "
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
			throw new DbConnectionException("Error retrieving user");
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
			throw new DbConnectionException("Error retrieving user email");
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
			throw new DbConnectionException("Error retrieving user password");
		}
	}

	@Override
	//nt
	public void deleteUser(long oldCreatorId, long newCreatorId, UserContextData context)
			throws DbConnectionException {
		Result<Void> result = self.deleteUserAndGetEvents(oldCreatorId, newCreatorId, context);
		eventFactory.generateAndPublishEvents(result.getEventQueue());
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

				result.appendEvents(assignNewOwner(newCreatorId, oldCreatorId, context).getEventQueue());
				User u = new User(oldCreatorId);
				//actor not passed
				result.appendEvent(eventFactory.generateEventData(EventType.Delete,
						context, u, null, null, null));
				//TODO check if line below is needed
				//userEntityESService.deleteNodeFromES(user);
				return result;
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Error", e);
				throw new DbConnectionException("Error deleting competences, credentials and activities of user");
			}
	}

	@Override
	@Transactional
	public void setUserOrganization(long userId, long organizationId) {
		try {
			User user = loadResource(User.class,userId);
			if (organizationId != 0) {
				user.setOrganization(loadResource(Organization.class, organizationId));
			} else {
				user.setOrganization(null);
			}
			saveEntity(user);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error setting organization for user");
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
			query += "ORDER BY user.lastname, user.name ASC ";

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
			throw new DbConnectionException("Error loading users");
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
		result.appendEvents(credentialManager.updateCredentialCreator(newCreatorId, oldCreatorId, context).getEventQueue());
		result.appendEvents(competence1Manager.updateCompetenceCreator(newCreatorId, oldCreatorId, context).getEventQueue());
		activity1Manager.updateActivityCreator(newCreatorId, oldCreatorId);
		return result;
	}

	@Override
	@Transactional (readOnly = true)
	public UserData getUserData(long id) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager()
					.get(User.class, id);
			return user != null ? new UserData(user) : null;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving user data");
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
			throw new DbConnectionException("Error retrieving unit users");
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
	public User createNewUserAndConnectToResources(
												   String name, String lastname, String emailAddress,
												   String password, String position, long unitId,
												   long unitRoleId, long userGroupId,
												   UserContextData context)
			throws DbConnectionException {
		Result<UserCreationData> res = self.createNewUserConnectToResourcesAndGetEvents(
				name, lastname, emailAddress, password, position, unitId, unitRoleId,
				userGroupId, context);

		if (res.getResult() != null) {
			eventFactory.generateAndPublishEvents(res.getEventQueue());
			return res.getResult().getUser();
		}
		return null;
	}

	@Override
	@Transactional
	public Result<UserCreationData> createNewUserConnectToResourcesAndGetEvents(
														   String name, String lastname, String emailAddress,
														   String password, String position, long unitId,
														   long unitRoleId, long userGroupId,
														   UserContextData context)
			throws DbConnectionException {
		try {
			Result<UserCreationData> res = new Result<>();

			Result<UserCreationData> newUserRes = createOrUpdateUser(
					name, lastname, emailAddress, true,
					password, position, false, null, null,
					unitRoleId, context);

			res.setResult(newUserRes.getResult());
			res.appendEvents(newUserRes.getEventQueue());

			//only if user is created/updated and retrieved successfully other operations can be performed
			if (newUserRes.getResult() != null) {
				if (unitId > 0 && unitRoleId > 0) {
					res.appendEvents(unitManager.addUserToUnitWithRoleAndGetEvents(
							newUserRes.getResult().getUser().getId(), unitId, unitRoleId, context).getEventQueue());
				}

				if (userGroupId > 0) {
					res.appendEvents(userGroupManager.addUserToTheGroupAndGetEvents(userGroupId, newUserRes.getResult().getUser().getId(),
							context).getEventQueue());
				}
			}
			persistence.currentManager().flush();

			return res;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw e;
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
	 * @return
	 * @throws DbConnectionException
	 */
	private Result<UserCreationData> createOrUpdateUser(String name, String lastname, String emailAddress, boolean emailVerified,
														String password, String position, boolean system, InputStream avatarStream,
														String avatarFilename, long roleId, UserContextData context) throws DbConnectionException {
		try {
			List<Long> roleIds = null;
			if (roleId > 0) {
				roleIds = new ArrayList<>();
				roleIds.add(roleId);
			}
			if (!checkIfUserExists(emailAddress)) {
				Result<User> newUserRes = createNewUserAndGetEvents(context.getOrganizationId(), name, lastname, emailAddress, emailVerified,
						password, position, avatarStream, avatarFilename, roleIds, system);
				Result<UserCreationData> res = new Result<>();
				res.setResult(new UserCreationData(newUserRes.getResult(), true));
				res.appendEvents(newUserRes.getEventQueue());
				return res;
			}else{
				Result<UserCreationData> res = new Result<>();
				/*
				TODO for now we only consider user if he is a part of the passed organization already
				That should be revisited when there can be two users with same email address and different
				organization
				 */
				User user = getUser(context.getOrganizationId(), emailAddress.toLowerCase());
				if (user != null) {
					if (user.isDeleted()) {
						res.appendEvents(activateUserAndUpdateBasicInfo(user, name, lastname, position, roleId,
								context).getEventQueue());
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
								res.appendEvent(eventFactory.generateEventData(
										EventType.USER_ROLES_UPDATED, context, us, null, null, null));
							}
						}
					}
				}

				return res;
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
														UserContextData context) {
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
		res.appendEvent(eventFactory.generateEventData(EventType.Account_Activated, context, u, null, null, null));

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public long getUserOrganizationId(long userId) throws DbConnectionException {
		try {
			String q =
					"SELECT user.organization.id " +
					"FROM User user " +
					"WHERE user.id = :userId";

			Long orgId = (Long) persistence.currentManager()
					.createQuery(q)
					.setLong("userId", userId)
					.uniqueResult();

			return orgId != null ? orgId : 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving user organization");
		}
	}

	@Override
	public void saveAccountChanges(UserData accountData, UserContextData contextData)
			throws DbConnectionException, ResourceCouldNotBeLoadedException {
		Result<Void> result = self.saveAccountChangesAndGetEvents(accountData,contextData);
		eventFactory.generateAndPublishEvents(result.getEventQueue());
	}

	@Override
	@Transactional (readOnly = false)
	public Result<Void> saveAccountChangesAndGetEvents(UserData accountData, UserContextData contextData)
			throws DbConnectionException {
		try {
			User user = loadResource(User.class, contextData.getActorId());

			user.setName(accountData.getName());
			user.setLastname(accountData.getLastName());
			user.setPosition(accountData.getPosition());

			if (accountData.getLocationName() == null ||
					accountData.getLocationName().isEmpty()) {
				user.setLocationName(null);
				user.setLatitude(null);
				user.setLongitude(null);
			} else {
				user.setLocationName(accountData.getLocationName());
				user.setLatitude(accountData.getLatitude());
				user.setLongitude(accountData.getLongitude());
			}

			Result<Void> result = new Result<>();

			saveEntity(user);
			//refresh user session data
			authenticatedUserService.refreshUserSessionData();

			result.appendEvent(eventFactory.generateEventData(EventType.Edit_Profile, contextData,
					null, null, null, null));

			return result;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error updating account data for user: " + accountData.getId());
		}
	}

	@Override
	@Transactional (readOnly = true)
	public UserAssessmentTokenData getUserAssessmentTokenData(long userId) {
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			boolean tokensEnabled = false;
			if (user.getOrganization() != null) {
				tokensEnabled = user.getOrganization().isAssessmentTokensEnabled();
			}
			return new UserAssessmentTokenData(tokensEnabled, user.isAvailableForAssessments(), user.getNumberOfTokens());
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error loading user assessment tokens data");
		}
	}

	@Override
	@Transactional (readOnly = true)
	public UserAssessmentTokenExtendedData getUserAssessmentTokenExtendedData(long userId) {
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			boolean tokensEnabled = false;
			int numberOfTokensSpentPerRequest = 0;
			if (user.getOrganization() != null) {
				tokensEnabled = user.getOrganization().isAssessmentTokensEnabled();
				numberOfTokensSpentPerRequest = user.getOrganization().getNumberOfSpentTokensPerRequest();
			}
			return new UserAssessmentTokenExtendedData(tokensEnabled, user.isAvailableForAssessments(), user.getNumberOfTokens(), numberOfTokensSpentPerRequest);
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error loading user assessment tokens data");
		}
	}

	@Override
	@Transactional
	public void updateAssessmentAvailability(long userId, boolean availableForAssessments) {
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			user.setAvailableForAssessments(availableForAssessments);
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error updating assessment availability");
		}
	}

}
