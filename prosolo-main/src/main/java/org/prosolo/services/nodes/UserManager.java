package org.prosolo.services.nodes;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;

public interface UserManager extends AbstractManager {

	User getUser(String email);

	boolean checkIfUserExists(String email);

	Collection<User> getAllUsers();
	
	User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
			String password, String position, InputStream avatarStream, 
			String avatarFilename, List<Long> roles) throws UserAlreadyRegisteredException, EventException;
	
	User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
			String password, String position, InputStream avatarStream, 
			String avatarFilename, List<Long> roles, boolean isSystem) throws UserAlreadyRegisteredException, EventException;

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
			String position, List<Long> roles, long creatorId) throws DbConnectionException, EventException;

	List<User> getUsers(Long[] toExclude, int limit);
	
	User getUserWithRoles(long id) throws DbConnectionException;
	
	String getUserEmail(long id) throws DbConnectionException;
	
	void deleteUser(long oldCreatorId, long newCreatorId) throws DbConnectionException, EventException;

	Result<Void> deleteUserAndGetEvents(long oldCreatorId, long newCreatorId) throws DbConnectionException;

	void setUserOrganization(long userId,long organizationId);
	PaginatedResult<UserData> getUsersWithRoles(int page, int limit, long roleId, List<Role> roles);

	String getUserPosition(long id);

}
