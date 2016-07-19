package org.prosolo.services.nodes.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.UserManager")
public class UserManagerImpl extends AbstractManagerImpl implements UserManager {
	
	private static final long serialVersionUID = 7695010428900106309L;
	
	private static Logger logger = Logger.getLogger(UserManager.class);
	
	@Autowired private PasswordEncrypter passwordEncrypter;
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
			String password, String position, InputStream avatarStream, 
			String avatarFilename) throws UserAlreadyRegisteredException, EventException {
		if (checkIfUserExists(emailAddress)) {
			throw new UserAlreadyRegisteredException("User with email address "+emailAddress+" is already registered.");
		}
		// it is called in a new transaction
		User newUser = resourceFactory.createNewUser(name, lastname, emailAddress, emailVerified, password, position, false, avatarStream, avatarFilename);
		
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
	public User changePassword(long userId, String newPassword) throws ResourceCouldNotBeLoadedException {
		User user = loadResource(User.class, userId);
		
		user.setPassword(passwordEncrypter.encodePassword(newPassword));
		user.setPasswordLength(newPassword.length());
		return saveEntity(user);
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
}
