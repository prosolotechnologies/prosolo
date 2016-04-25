package org.prosolo.services.nodes.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.preferences.UserPreference;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.authentication.PasswordEncrypter;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.prosolo.services.upload.AvatarProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.UserManager")
public class UserManagerImpl extends AbstractManagerImpl implements UserManager {
	
	private static final long serialVersionUID = 7695010428900106309L;
	
	private static Logger logger = Logger.getLogger(UserManager.class);
	
	@Autowired private PasswordEncrypter passwordEncrypter;
	@Autowired private AvatarProcessor avatarProcessor; 
	@Autowired private EventFactory eventFactory;
	@Autowired private ResourceFactory resourceFactory;
	 
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
			String password, String position, InputStream avatar, 
			String avatarFilename) throws UserAlreadyRegisteredException, EventException {
		User newUser = createNewUser(name, lastname, emailAddress, emailVerified, password, position);
		newUser = updateUserAvatar(newUser, avatar, avatarFilename);
		return newUser;
	}
	
	@Override
	//@Transactional (readOnly = false)
	public User createNewUser(String name, String lastname,	String emailAddress, boolean emailVerified, 
			String password, String position) 
					throws UserAlreadyRegisteredException, EventException {
		if (checkIfUserExists(emailAddress)) {
			throw new UserAlreadyRegisteredException("User with email address "+emailAddress+" is already registered.");
		}
		// it is called in a new transaction
		User newUser = resourceFactory.createNewUser(name, lastname, emailAddress, emailVerified, password, position, false);
		eventFactory.generateEvent(EventType.Registered, newUser);
		
		return newUser;
	}

	@Override
	@Transactional (readOnly = false)
	public User updateUserAvatar(User user, InputStream imageInputStream, String avatarFilename){
		if (imageInputStream != null) {
			try {
			//	user = merge(user);
				user.setAvatarUrl(avatarProcessor.storeUserAvatar(user, imageInputStream, avatarFilename, true));
				return saveEntity(user);
			} catch (IOException e) {
				logger.error(e);
			}
		} 
		return user;
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
	public User changePassword(User user, String newPassword) {
		user.setPassword(passwordEncrypter.encodePassword(newPassword));
		user.setPasswordLength(newPassword.length());
		return saveEntity(user);
	}
	
	@Override
	@Transactional (readOnly = false)
	public User changeAvatar(User user, String newAvatarPath) {
	//	user = merge(user);
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
			String position) throws ResourceCouldNotBeLoadedException {
		
		User user = loadResource(User.class, userId);
		user.setName(name);
		user.setLastname(lastName);
		user.setPosition(position);
		user.setEmail(email);
		user.setVerified(true);
		
		if (changePassword) {
			user.setPassword(passwordEncrypter.encodePassword(password));
			user.setPasswordLength(password.length());
		}
		
		return saveEntity(user);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Long> getUsers(List<Long> toExclude) {
		StringBuffer query = new StringBuffer();
		
		query.append(	
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted "
		);
		
		if (toExclude != null && !toExclude.isEmpty()) {
			query.append(
					"AND user.id NOT IN (:excludeIds) "
			);
		}
		
		Query q = persistence.currentManager().createQuery(query.toString()).
					setBoolean("deleted", false);
		
		if (toExclude != null && !toExclude.isEmpty()) {
			q.setParameterList("excludeIds", toExclude);
		}
		logger.debug("Query:"+query +" exludeIds:"+toExclude.toString());
		@SuppressWarnings("unchecked")
		List<Long> result = q.list();
		
		if (result != null) {
  			return result;
		}

		return new ArrayList<Long>();
	}
}
