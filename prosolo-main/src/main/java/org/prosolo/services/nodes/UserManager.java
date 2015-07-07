package org.prosolo.services.nodes;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.services.event.EventException;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;

public interface UserManager extends AbstractManager {

	User getUser(String defaultEmail);

	boolean checkIfUserExists(String email);

	Collection<User> getAllUsers();
	
	User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, String password, Organization organization, String position) throws UserAlreadyRegisteredException, EventException;

	User createNewUser(String name, String lastname, String emailAddress, boolean emailVerified, String password, Organization organization, String position, InputStream avatar, String avatarName) throws UserAlreadyRegisteredException, EventException;

	User updateUserAvatar(User user, InputStream imageInputStream, String originalName);
	
	void addTopicPreferences(User user, Collection<Tag> tags);
	
	User changePassword(User user, String newPassword);
	
	Collection<Email> getEmails(User user);

	User changeAvatar(User user, String newAvatarPath);

	List<User> loadUsers(List<Long> ids);

	UserPreference getUserPreferences(User user,
			Class<? extends UserPreference> preferenceClass);

	User updateUser(long userId, String name, String lastName, String email, boolean emailVerified, 
			boolean changePassword, String password, String position) throws ResourceCouldNotBeLoadedException;

	List<Long> getUsers(List<Long> toExclude);

}
