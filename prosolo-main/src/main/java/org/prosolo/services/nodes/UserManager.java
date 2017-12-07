package org.prosolo.services.nodes;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UserCreationData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface UserManager extends AbstractManager {

	User getUser(String email) throws DbConnectionException;

	User getUserIfNotDeleted(String email) throws DbConnectionException;

	User getUser(long organizationId, String email) throws DbConnectionException;

	boolean checkIfUserExists(String email) throws DbConnectionException;

	boolean checkIfUserExistsAndNotDeleted(String email) throws DbConnectionException;

	Collection<User> getAllUsers(long orgId);

	Collection<User> getAllUsers(long orgId, Session session) throws DbConnectionException;
	
	User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream, 
			String avatarFilename, List<Long> roles) throws UserAlreadyRegisteredException;
	
	User createNewUser(long organizationId, String name, String lastname, String emailAddress, boolean emailVerified,
			String password, String position, InputStream avatarStream, 
			String avatarFilename, List<Long> roles, boolean isSystem) throws UserAlreadyRegisteredException;

	void addTopicPreferences(User user, Collection<Tag> tags);
	
	String getPassword(long userId) throws ResourceCouldNotBeLoadedException;
	
	String changePassword(long userId, String newPassword) throws ResourceCouldNotBeLoadedException;
	
	String changePasswordWithResetKey(String resetKey, String newPassword);
	
	User changeAvatar(long userId, String newAvatarPath) throws ResourceCouldNotBeLoadedException;

	List<User> loadUsers(List<Long> ids);

	UserPreference getUserPreferences(User user,
			Class<? extends UserPreference> preferenceClass);

	User updateUser(long userId, String name, String lastName, String email,
			boolean emailVerified, boolean changePassword, String password, 
			String position, List<Long> roles, List<Long> rolesToUpdate, UserContextData context)
			throws DbConnectionException;

	List<User> getUsers(Long[] toExclude, int limit);

	/**
	 *
	 * @param id
	 * @param organizationid if greater than 0, user will be returned only if it is assigned to organization specified by id,
	 *                       otherwise user will be returned no matter which organization he belongs to
	 * @return
	 * @throws DbConnectionException
	 */
	UserData getUserWithRoles(long id, long organizationid) throws DbConnectionException;
	
	String getUserEmail(long id) throws DbConnectionException;
	
	void deleteUser(long oldCreatorId, long newCreatorId, UserContextData context) throws DbConnectionException;

	Result<Void> deleteUserAndGetEvents(long oldCreatorId, long newCreatorId, UserContextData context)
			throws DbConnectionException;

	void setUserOrganization(long userId,long organizationId);

	/**
	 *
	 * @param page
	 * @param limit
	 * @param roleId
	 * @param roles
	 * @param organizationId - if less than or equals 0 users from all organizations will be returned
	 * @return
	 * @throws DbConnectionException
	 */
	PaginatedResult<UserData> getUsersWithRoles(int page, int limit, long roleId, List<Role> roles, long organizationId)
			throws DbConnectionException;

	void setOrganizationForUsers(List<UserData> users,Long organizationId);

	UserData getUserData(long id) throws DbConnectionException;

	PaginatedResult<UserData> getPaginatedOrganizationUsersWithRoleNotAddedToUnit(
			long orgId, long unitId, long roleId, int offset, int limit) throws DbConnectionException;

	Result<UserCreationData> createNewUserConnectToResourcesAndGetEvents(
															 String name, String lastname, String emailAddress,
															 String password, String position, long unitId,
															 long unitRoleId, long userGroupId,
															 UserContextData context)
			throws DbConnectionException;

	/**
	 * Creates account for a users if it does not exist, recreates deleted user account and if user exists
	 * it only updates his roles (adds role with {@code unitRoleId} id if not already added.
	 *
	 * Also it adds user to the unit with {@code unitId} id with role ({@code unitRoleId}) if {@code unitId}
	 * and {@code unitRoleId} are greater than 0; adds user to the user group ({@code userGroupId})
	 * if {@code userGroupId} is greater than 0.
	 *
	 * @param name
	 * @param lastname
	 * @param emailAddress
	 * @param password
	 * @param position
	 * @param unitId
	 * @param unitRoleId
	 * @param userGroupId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	boolean createNewUserAndConnectToResources(
											String name, String lastname, String emailAddress,
											String password, String position, long unitId,
											long unitRoleId, long userGroupId, UserContextData context)
			throws DbConnectionException;

	long getUserOrganizationId(long userId) throws DbConnectionException;

	void saveAccountChanges(UserData accountData, UserContextData contextData)
			throws DbConnectionException, EventException, ResourceCouldNotBeLoadedException;

	Result<Void> saveAccountChangesAndGetEvents(UserData accountData, long userId, UserContextData contextData)
			throws DbConnectionException, EventException, ResourceCouldNotBeLoadedException;
}
