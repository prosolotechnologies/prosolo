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
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.users.UserScopeFilter;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.user.data.UserData;
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
				
				FollowedEntity followedEntity = new FollowedUserEntity();
				followedEntity.setUser(follower);
				followedEntity.setFollowedResource(userToFollow);
				followedEntity.setStartedFollowing(new Date());
				persistence.currentManager().saveOrUpdate(followedEntity);
				
				persistence.currentManager().flush();

				Result<User> res = new Result<>();
				res.setResult(follower);
				User eventObject = new User();
				eventObject.setId(userToFollowId);
				res.appendEvent(eventFactory.generateEventData(EventType.Follow, context, eventObject, null, null, null));
				
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
			"LEFT JOIN fEnt.user user " +
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

			User userToUnfollow = loadResource(User.class, userToUnfollowId);

			Result<Boolean> res = new Result<>();
			res.setResult(deleted > 0);
			if (deleted > 0) {
				User eventObject = new User();
				eventObject.setId(userToUnfollowId);
				res.appendEvent(eventFactory.generateEventData(EventType.Unfollow, context, eventObject, null, null, null));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error unfollowing the user");
		}
	}

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<UserData> getPaginatedUsersWithFollowInfo(long userId, UserSearchConfig searchConfig, int page, int limit) throws DbConnectionException {
        switch (searchConfig.getScope()) {
            case ORGANIZATION:
                return getPaginatedOtherUsersFromOrganizationWithFollowInfo(userId, searchConfig, page, limit);
            case FOLLOWING:
                return getPaginatedFollowingUsers(userId, searchConfig, page, limit);
            case FOLLOWERS:
                return getPaginatedFollowers(userId, searchConfig, page, limit);
        }
        return null;
    }

	private PaginatedResult<UserData> getPaginatedFollowingUsers(long userId, UserSearchConfig searchConfig, int page, int limit) throws DbConnectionException {
		try {
			PaginatedResult<UserData> res = new PaginatedResult<>();
			res.setHitsNumber(getNumberOfFollowingUsers(userId, searchConfig));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getFollowingUsers(userId, searchConfig, page, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving users a given user follows");
		}
	}
	
	private List<UserData> getFollowingUsers(long userId, UserSearchConfig searchConfig, int page, int limit) {
		StringBuilder query = new StringBuilder(
			"SELECT DISTINCT fUser " +
			"FROM FollowedEntity fEnt " +
			"INNER JOIN fEnt.user user "+
			"JOIN fEnt.followedUser fUser " +
			"INNER JOIN fUser.roles role " +
			"WITH role.id = :roleId " +
			"WHERE user.id = :userId ");

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return empty list as a result without issuing the query
			if (searchConfig.getUnitIds().isEmpty()) {
				return new ArrayList<>();
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = fUser.id AND urm.unit.id IN (:unitIds))");
		}

		query.append("ORDER BY fUser.lastname, fUser.name");

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", searchConfig.getRoleId());

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", searchConfig.getUnitIds());
		}

		if (limit != 0) {
			q.setFirstResult(page * limit)
					.setMaxResults(limit);
		}

		List<User> users = q.list();

		List<UserData> res = new ArrayList<>();
		for (User user : users) {
			UserData userData = new UserData(user);
			//we only retrieve users that are followed by given user, so this property should be true for all users
			userData.setFollowedByCurrentUser(true);
			res.add(userData);
		}

		return res;
	}

	private int getNumberOfFollowingUsers(long userId, UserSearchConfig userSearchConfig) throws DbConnectionException {
		StringBuilder query = new StringBuilder(
				"SELECT cast( COUNT(DISTINCT fUser) as int) " +
						"FROM FollowedEntity fEnt " +
						"LEFT JOIN fEnt.user user "+
						"JOIN fEnt.followedUser fUser " +
						"INNER JOIN fUser.roles role " +
						"WITH role.id = :roleId " +
						"WHERE user.id = :userId ");

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return 0 as a result without issuing the query
			if (userSearchConfig.getUnitIds().isEmpty()) {
				return 0;
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = fUser.id AND urm.unit.id IN (:unitIds))");
		}

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", userSearchConfig.getRoleId());

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", userSearchConfig.getUnitIds());

		}

		return (int) q.uniqueResult();
	}

	private PaginatedResult<UserData> getPaginatedOtherUsersFromOrganizationWithFollowInfo(long userId, UserSearchConfig searchConfig, int page, int limit) throws DbConnectionException {
		try {
			PaginatedResult<UserData> res = new PaginatedResult<>();
			res.setHitsNumber(getNumberOfOtherUsersFromOrganization(userId, searchConfig));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getOtherUsersFromOrganizationWithFollowInfo(userId, searchConfig, page, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving other users from organization");
		}
	}

	private List<UserData> getOtherUsersFromOrganizationWithFollowInfo(long userId, UserSearchConfig searchConfig, int page, int limit) {
		StringBuilder query = new StringBuilder(
				"SELECT user " +
				   "FROM User user " +
				   "INNER JOIN user.roles role " +
				   "WITH role.id = :roleId " +
				   "WHERE user.id != :userId " +
				   "AND user.organization.id = (SELECT u.organization.id FROM User u WHERE u.id = :userId) ");

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return empty list as a result without issuing the query
			if (searchConfig.getUnitIds().isEmpty()) {
				return new ArrayList<>();
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = user.id AND urm.unit.id IN (:unitIds))");
		}

		query.append("ORDER BY user.lastname, user.name");

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", searchConfig.getRoleId());

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", searchConfig.getUnitIds());
		}

		if (limit != 0) {
			q.setFirstResult(page * limit)
					.setMaxResults(limit);
		}

		List<User> users = q.list();

		List<UserData> res = new ArrayList<>();
		for (User user : users) {
			UserData userData = new UserData(user);
			userData.setFollowedByCurrentUser(isUserFollowingUser(userId, userData.getId()));
			res.add(userData);
		}

		return res;
	}

	private int getNumberOfOtherUsersFromOrganization(long userId, UserSearchConfig userSearchConfig) throws DbConnectionException {
		StringBuilder query = new StringBuilder(
				"SELECT cast( COUNT(user) as int) " +
						"FROM User user " +
						"INNER JOIN user.roles role " +
						"WITH role.id = :roleId " +
						"WHERE user.id != :userId " +
						"AND user.organization.id = (SELECT u.organization.id FROM User u WHERE u.id = :userId) ");

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return 0 as a result without issuing the query
			if (userSearchConfig.getUnitIds().isEmpty()) {
				return 0;
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = user.id AND urm.unit.id IN (:unitIds))");
		}

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", userSearchConfig.getRoleId());

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", userSearchConfig.getUnitIds());

		}

		return (int) q.uniqueResult();
	}

	private PaginatedResult<UserData> getPaginatedFollowers(long userId, UserSearchConfig searchConfig, int page, int limit) throws DbConnectionException {
		try {
			PaginatedResult<UserData> res = new PaginatedResult<>();
			res.setHitsNumber(getNumberOfFollowers(userId, searchConfig));
			if (res.getHitsNumber() > 0) {
				res.setFoundNodes(getFollowers(userId, searchConfig, page, limit));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving users a given user follows");
		}
	}

	private List<UserData> getFollowers(long userId, UserSearchConfig searchConfig, int page, int limit) {
		StringBuilder query = new StringBuilder(
			 "SELECT user " +
				"FROM FollowedEntity fEnt " +
				"INNER JOIN fEnt.user user " +
			    "INNER JOIN user.roles role " +
			    "WITH role.id = :roleId " +
				"INNER JOIN fEnt.followedUser fUser " +
				"WHERE fUser.id = :userId ");

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return empty list as a result without issuing the query
			if (searchConfig.getUnitIds().isEmpty()) {
				return new ArrayList<>();
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = user.id AND urm.unit.id IN (:unitIds))");
		}

		query.append("ORDER BY user.lastname, user.name");

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", searchConfig.getRoleId());

		if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", searchConfig.getUnitIds());
		}

		if (limit != 0) {
			q.setFirstResult(page * limit)
					.setMaxResults(limit);
		}

		List<User> users = q.list();

		List<UserData> res = new ArrayList<>();
		for (User user : users) {
			UserData userData = new UserData(user);
			//we only retrieve users that are followed by given user, so this property should be true for all users
			userData.setFollowedByCurrentUser(isUserFollowingUser(userId, userData.getId()));
			res.add(userData);
		}

		return res;
	}

	private int getNumberOfFollowers(long userId, UserSearchConfig userSearchConfig) throws DbConnectionException {
		StringBuilder query = new StringBuilder(
				"SELECT cast( COUNT(user) as int) " +
						"FROM FollowedEntity fEnt " +
						"INNER JOIN fEnt.user user " +
						"INNER JOIN user.roles role " +
						"WITH role.id = :roleId " +
						"INNER JOIN fEnt.followedUser fUser " +
						"WHERE fUser.id = :userId ");

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			//if units list passed is empty we can return 0 as a result without issuing the query
			if (userSearchConfig.getUnitIds().isEmpty()) {
				return 0;
			}
			query.append("AND exists (SELECT urm.id FROM UnitRoleMembership urm WHERE urm.role.id = :roleId AND urm.user = user.id AND urm.unit.id IN (:unitIds))");
		}

		Query q = persistence.currentManager().createQuery(query.toString())
				.setLong("userId", userId)
				.setLong("roleId", userSearchConfig.getRoleId());

		if (userSearchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
			q.setParameterList("unitIds", userSearchConfig.getUnitIds());

		}

		return (int) q.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<User> getFollowers(long userId) throws DbConnectionException {
		try {
			String query = 
					"SELECT DISTINCT user " + 
					"FROM FollowedEntity fEnt " + 
					"LEFT JOIN fEnt.user user "+
					"JOIN fEnt.followedUser fUser " + 
					"WHERE fUser.id = :userId " +
					"ORDER BY user.name, user.lastname";
			
			Query q = persistence.currentManager().createQuery(query)
					.setLong("userId", userId);
			
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
	
}
