package org.prosolo.services.nodes;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;

public interface UserManager extends AbstractManager {

	User getUser(String email);

	boolean checkIfUserExists(String email);

	Collection<User> getAllUsers();
	
	User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, 
			String password, String position, InputStream avatarStream, 
			String avatarFilename, List<Long> roles) throws UserAlreadyRegisteredException, EventException;

	void addTopicPreferences(User user, Collection<Tag> tags);
	
	User changePassword(long userId, String newPassword) throws ResourceCouldNotBeLoadedException;
	
	User changeAvatar(long userId, String newAvatarPath) throws ResourceCouldNotBeLoadedException;

	List<User> loadUsers(List<Long> ids);

	UserPreference getUserPreferences(User user,
			Class<? extends UserPreference> preferenceClass);

	User updateUser(long userId, String name, String lastName, String email,
			boolean emailVerified, boolean changePassword, String password, 
			String position, List<Long> roles, long creatorId) throws DbConnectionException, EventException;

	List<User> getUsers(Long[] toExclude, int limit);
	
	User getUserWithRoles(long id) throws DbConnectionException;

}
