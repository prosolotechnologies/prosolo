package org.prosolo.services.lti.impl;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.common.domainmodel.lti.LtiConsumer;
import org.prosolo.common.domainmodel.lti.LtiUser;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.LtiUserManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.exceptions.UserAlreadyRegisteredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.lti.LtiUserManager")
public class LtiUserManagerImpl extends AbstractManagerImpl implements LtiUserManager {

	private static final long serialVersionUID = -5866975520782311614L;

	private static Logger logger = Logger.getLogger(LtiUserManager.class);

	@Inject
	private UserManager userManager;

	@Override
	@Transactional
	public User getUserForLaunch(long consumerId, String userId, String name, String lastName, String email, long courseId)
			throws DbConnectionException {
		try {
			User user = getUser(consumerId, userId);
			if (user == null) {
				LtiConsumer consumer = (LtiConsumer) persistence.currentManager().load(LtiConsumer.class, consumerId);
				LtiUser ltiUser = new LtiUser();
				ltiUser.setUserId(userId);
				ltiUser.setConsumer(consumer);
				user = createOrGetExistingUser(name, lastName, email);
				ltiUser.setUser(user);
				saveEntity(ltiUser);
			}
			return user;
		} catch (Exception e) {
			throw new DbConnectionException("Error while logging user in");
		}
	}

	private User createOrGetExistingUser(String name, String lastName, String email) throws DbConnectionException{
		try{
			User user = null;
			String password = UUID.randomUUID().toString();
			try {
				user = userManager.createNewUser(name, lastName, email, true, password, null);
			} catch (UserAlreadyRegisteredException e) {
				user = userManager.getUser(email);
			} catch (EventException e) {
				logger.error(e);
			}
			return user;
		}catch(Exception e){
			throw new DbConnectionException("User cannot be retrieved");
		}
	}

	private User getUser(long consumerId, String userId) {
		try{
			String queryString = "SELECT user " + "FROM LtiUser ltiuser " + "INNER JOIN  ltiuser.user user "
					+ "INNER JOIN ltiuser.consumer c " + "WHERE ltiuser.userId = :userId " + "AND c.id = :id";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", consumerId);
			query.setString("userId", userId);
	
			return (User) query.uniqueResult();
		}catch(Exception e){
			throw new DbConnectionException("User cannot be retrieved");
		}

	}

}
