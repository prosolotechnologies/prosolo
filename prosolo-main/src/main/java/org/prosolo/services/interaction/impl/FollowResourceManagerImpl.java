package org.prosolo.services.interaction.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.following.FollowedEntity;
import org.prosolo.common.domainmodel.user.following.FollowedUserEntity;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.FollowResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 *
 */
@Transactional
@Service("org.prosolo.services.interaction.FollowResourceManager")
public class FollowResourceManagerImpl extends AbstractManagerImpl implements FollowResourceManager, Serializable {

	private static final long serialVersionUID = -5774069193905411157L;

	private static Logger logger = Logger.getLogger(FollowResourceManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;

//	@Override
//	@Transactional
//	public User followResource(User user, Node resourceToFollow, String context) throws EventException {
//		if (resourceToFollow != null && user != null) {
//			FollowedEntity followedEntity = new FollowedResourceEntity();
//			followedEntity.setUser(user);
//			followedEntity.setFollowedResource(resourceToFollow);
//			followedEntity.setStartedFollowing(new Date());
//			followedEntity = saveEntity(followedEntity);
//			
//			logger.debug(user.getName() + " started following "	+ resourceToFollow.getId());
//			
//			Map<String, String> parameters = new HashMap<String, String>();
//			parameters.put("context", context);
//			
//			eventFactory.generateEvent(EventType.Follow, user.getId(), resourceToFollow, null, parameters);
//
//			return user;
//		}
//		return null;
//	}
	
	@Override
	@Transactional 
	public User followUser(long followerId, long userToFollowId, String context) throws EventException, ResourceCouldNotBeLoadedException{
		if (userToFollowId > 0 && followerId > 0) {
			User follower = loadResource(User.class, followerId);
			User userToFollow = loadResource(User.class, userToFollowId);
			
			FollowedEntity followedEntity = new FollowedUserEntity();
			followedEntity.setUser(follower);
			followedEntity.setFollowedResource(userToFollow);
			followedEntity.setStartedFollowing(new Date());
			followedEntity = saveEntity(followedEntity);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.Follow, followerId, userToFollow, null, parameters);
			
			logger.debug(follower.getName() + " started following user " + userToFollow.getId());
			return follower;
		}
		return null;
	}
	
//	@Override
//	@Transactional (readOnly = true)
//	public FollowedEntity getFollowedEntity(User user, Node followedResource, Session session) {
//		String query = 
//			"SELECT DISTINCT followedEnt " +
//			"FROM FollowedEntity followedEnt " +
//			"LEFT JOIN followedEnt.user user " +
//			"WHERE followedEnt.followedNode = :resource " +
//				"AND user = :user ";
//		
//		FollowedEntity result = (FollowedEntity) session.createQuery(query).
//			setEntity("user", user).
//			setEntity("resource", followedResource).
//			uniqueResult();
//		
//		if (result != null) {
//			return result;
//		}
//		return null;
//	}
//	
//	@Override
//	@Transactional (readOnly = true)
//	public List<User> getUserFollowers(User user) {
//		return getUserFollowers(user.getId());
//	}
//	
//	@Override
//	@Transactional (readOnly = true)
//	public List<User> getUserFollowers(User user, Session session) {
//		return getUserFollowers(user.getId(), session);
//	}
	
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

//	@Override
//	@Transactional (readOnly = true)
//	public Collection<User> getResourceFollowers(BaseEntity resource, Session session) {
//		String query = 
//			"SELECT DISTINCT user "	+ 
//			"FROM FollowedEntity fEnt " + 
//			"LEFT JOIN fEnt.user user "+
//			"LEFT JOIN fEnt.followedNode followedRes " + 
//			"WHERE followedRes = :resource";
//		
//		@SuppressWarnings("unchecked")
//		Collection<User> followers = session.createQuery(query)
//			.setEntity("resource", resource)
//			.list();
//
//		if (followers != null) {
//			return followers;
//		}
//		return new ArrayList<User>();
//	}
	
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
	
//	@Override
//	@Transactional (readOnly = true)
//	public boolean isUserFollowingResource(User followerUser, Node followedResource){
//		String query = 
//			"SELECT cast(COUNT(DISTINCT fEnt) as int) "+
//			"FROM FollowedEntity fEnt " + 
//			"LEFT JOIN fEnt.user user "+
//			"WHERE fEnt.followedNode = :resource " +
//				"AND user = :user ";
//		
//		Integer followedEntNo = (Integer) persistence.currentManager().createQuery(query)
//				.setEntity("user", followerUser)
//				.setEntity("resource", followedResource)
//				.uniqueResult();
// 		return followedEntNo> 0;
//	}
	
	@Override 
	@Transactional (readOnly = true)
	public boolean isUserFollowingUser(long followerUserId, long followedUserId){
		String query = 
			"SELECT cast(COUNT(DISTINCT fEnt) as int) "+
			"FROM FollowedEntity fEnt " + 
			"LEFT JOIN fEnt.user user "+
			"WHERE fEnt.followedUser.id = :followedUserId " +
				"AND user.id = :userId ";
		
		Integer followedEntNo = (Integer) persistence.currentManager().createQuery(query)
			.setLong("userId", followerUserId)
			.setLong("followedUserId", followedUserId)
			.uniqueResult();
		
 		return followedEntNo> 0;
	}
	
	@Override
	@Transactional 
	public boolean unfollowUser(long followerId, long userToUnfollowId, String context) throws EventException {
		String query = 
			"DELETE FROM FollowedUserEntity fEnt " +
			"WHERE fEnt.user.id = :followerId " +
				"AND fEnt.followedUser.id = :userToUnfollowId";
		
		int deleted = persistence.currentManager().createQuery(query)
			.setLong("followerId", followerId)
			.setLong("userToUnfollowId", userToUnfollowId)
			.executeUpdate();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		try {
			User userToUnfollow = loadResource(User.class, userToUnfollowId);
			
			eventFactory.generateEvent(EventType.Unfollow, followerId, userToUnfollow, null, parameters);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		return deleted > 0;
	}
	
//	@Override
//	@Transactional 
//	public boolean unfollowResource(User user, Node resourceToUnfollow, String context) throws EventException {
//		if (resourceToUnfollow != null && user != null) {
//			String query = 
//				"DELETE FROM FollowedResourceEntity fEnt " +
//				"WHERE fEnt.user = :user " +
//					"AND fEnt.followedNode = :resourceToUnfollow";
//			
//			int deleted = persistence.currentManager().createQuery(query)
//				.setEntity("user", user)
//				.setEntity("resourceToUnfollow", resourceToUnfollow)
//				.executeUpdate();
//			
//			Map<String, String> parameters = new HashMap<String, String>();
//			parameters.put("context", context);
//			
//			eventFactory.generateEvent(EventType.Unfollow, user.getId(), resourceToUnfollow, null, parameters);
//			
//			return deleted > 0;
//		}
//		return false;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException {
		try {
			String query = 
					"SELECT DISTINCT fUser " + 
					"FROM FollowedEntity fEnt " + 
					"LEFT JOIN fEnt.user user "+
					"JOIN fEnt.followedUser fUser " + 
					"WHERE user = :user " +
					"ORDER BY fUser.name, fUser.lastname";
			
			Query q = persistence.currentManager().createQuery(query)
					.setLong("userId", userId);
			
			if(limit != 0) {
				q.setFirstResult(page * limit)
				.setMaxResults(limit);
			}
			
			List<User> users = q.list();
			
			if (users != null) {
				return users;
			}
			return new ArrayList<User>();
		} catch(DbConnectionException e) {
			logger.error(e);
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
