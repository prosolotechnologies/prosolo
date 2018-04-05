package org.prosolo.services.interaction.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.following.FollowedEntity;
import org.prosolo.common.domainmodel.user.following.FollowedUserEntity;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.FollowResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.interaction.FollowResourceManager")
public class FollowResourceManagerImpl extends AbstractManagerImpl implements FollowResourceManager, Serializable {

	private static final long serialVersionUID = -5774069193905411157L;

	private static Logger logger = Logger.getLogger(FollowResourceManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	@Inject private FollowResourceManager self;

	@Override
	public User followUser(long userToFollowId, UserContextData context)
			throws DbConnectionException, EntityAlreadyExistsException {
		Result<User> res = self.followUserAndGetEvents(userToFollowId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<User> followUserAndGetEvents(long userToFollowId, UserContextData context)
			throws DbConnectionException, EntityAlreadyExistsException {
		try {
			if (userToFollowId > 0 && context.getActorId() > 0) {
				User follower = loadResource(User.class, context.getActorId());
				User userToFollow = loadResource(User.class, userToFollowId);
				
				FollowedUserEntity followedEntity = new FollowedUserEntity();
				followedEntity.setUser(follower);
				followedEntity.setFollowedUser(userToFollow);
				followedEntity.setStartedFollowing(new Date());
				saveEntity(followedEntity);

				Result<User> res = new Result<>();
				res.setResult(follower);
				User userToFollowObj = new User();
				userToFollowObj.setId(userToFollowId);
				res.appendEvent(eventFactory.generateEventData(EventType.Follow, context, userToFollowObj, null, null, null));
				
				logger.debug(follower.getName() + " started following user " + userToFollow.getId());
				return res;
			}
			return new Result<>();
		} catch(ConstraintViolationException ex) {
			throw new EntityAlreadyExistsException();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while trying to follow user");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUserFollowers(long userId) {
		return getUserFollowers(userId, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUserFollowers(long userId, Session session) {
		String query = 
			"SELECT distinct user " + 
			"FROM FollowedEntity fEnt " + 
			"LEFT JOIN fEnt.user user "+
			"LEFT JOIN fEnt.followedUser fUser " + 
			"WHERE fUser.id = :userId";
		
		@SuppressWarnings("unchecked")
		List<User> users = session.createQuery(query)
			.setLong("userId", userId)
			.list();
		
		if (users != null) {
			return users;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional 
	public List<User> getFollowingUsers(long userId) throws DbConnectionException{
		try {
			String query = 
				"SELECT DISTINCT fUser " + 
				"FROM FollowedEntity fEnt " + 
				"LEFT JOIN fEnt.user user "+
				"JOIN fEnt.followedUser fUser " + 
				"WHERE user.id = :userId " +
				"ORDER BY fUser.name, fUser.lastname";
		
			@SuppressWarnings("unchecked")
			List<User> users = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.list();
			
			if (users != null) {
				return users;
			}
		} catch(Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while retrieving follwing users");
		}
		return new ArrayList<User>();
	}
	
	@Override 
	@Transactional (readOnly = true)
	public boolean isUserFollowingUser(long followerUserId, long followedUserId){
		String query = 
			"SELECT cast(COUNT(fEnt.id) as int) "+
			"FROM FollowedEntity fEnt " + 
			"LEFT JOIN fEnt.user user "+
			"WHERE fEnt.followedUser.id = :followedUserId " +
				"AND user.id = :userId ";
		
		Integer followedEntNo = (Integer) persistence.currentManager().createQuery(query)
			.setLong("userId", followerUserId)
			.setLong("followedUserId", followedUserId)
			.uniqueResult();
		
 		return followedEntNo == 1;
	}

	@Override
	public boolean unfollowUser(long userToUnfollowId, UserContextData context) throws DbConnectionException {
		Result<Boolean> res = self.unfollowUserAndGetEvents(userToUnfollowId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional 
	public Result<Boolean> unfollowUserAndGetEvents(long userToUnfollowId, UserContextData context) throws DbConnectionException {
		try {
			String query =
					"DELETE FROM FollowedUserEntity fEnt " +
					"WHERE fEnt.user.id = :followerId " +
					"AND fEnt.followedUser.id = :userToUnfollowId";

			int deleted = persistence.currentManager().createQuery(query)
					.setLong("followerId", context.getActorId())
					.setLong("userToUnfollowId", userToUnfollowId)
					.executeUpdate();

			Result<Boolean> res = new Result<>();
			res.setResult(deleted > 0);
			if (deleted > 0) {
				User userToUnfollowObj = new User();
				userToUnfollowObj.setId(userToUnfollowId);
				res.appendEvent(eventFactory.generateEventData(EventType.Unfollow, context, userToUnfollowObj, null, null, null));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error unfollowing the user");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException {
		try {
			String query = 
				"SELECT DISTINCT fUser " + 
				"FROM FollowedEntity fEnt " + 
				"LEFT JOIN fEnt.user user "+
				"JOIN fEnt.followedUser fUser " + 
				"WHERE user.id = :userId " +
				"ORDER BY fUser.lastname, fUser.name";
			
			Query q = persistence.currentManager().createQuery(query)
					.setLong("userId", userId);
			
			if (limit != 0) {
				q.setFirstResult(page * limit)
						.setMaxResults(limit);
			}
			
			List<User> users = q.list();
			
			if (users != null) {
				return users;
			}
			return new ArrayList<User>();
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException("Error while retrieving notification data");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getFollowers(long userId, Session session) throws DbConnectionException {
		try {
			String query = 
					"SELECT DISTINCT user " + 
					"FROM FollowedUserEntity fEnt " +
					"LEFT JOIN fEnt.user user "+
					"JOIN fEnt.followedUser fUser " + 
					"WHERE fUser.id = :userId " +
					"ORDER BY user.name, user.lastname";
			
			Query q = session.createQuery(query)
					.setLong("userId", userId);
			
			List<User> users = q.list();
			
			if (users != null) {
				return users;
			}
			return new ArrayList<>();
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving notification data");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public int getNumberOfFollowingUsers(long userId) throws DbConnectionException {
		Integer resNumber = 0;
		try {
			String query = 
				"SELECT cast( COUNT(DISTINCT fUser) as int) " + 
				"FROM FollowedEntity fEnt " + 
				"LEFT JOIN fEnt.user user "+
				"JOIN fEnt.followedUser fUser " + 
				"WHERE user.id = :userId ";
	
		resNumber = (Integer) persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.uniqueResult();

		} catch(Exception e) {
			throw new DbConnectionException("Error while retrieving follwing users");
		}
		return resNumber;
	}
	
}
