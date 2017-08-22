/**
 * 
 */
package org.prosolo.services.interaction;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.event.EventException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Nikola Milikic
 *
 */
public interface FollowResourceManager extends AbstractManager {

	User followUser(long follower, long userToFollowId, String context) throws EventException, ResourceCouldNotBeLoadedException;
	
	User followUser(long follower, long userToFollowId, UserContextData learningContext) throws DbConnectionException, EntityAlreadyExistsException;
	
	boolean unfollowUser(long followerId, long userToUnfollow, String context) throws EventException;

	boolean unfollowUser(long followerId, long userToUnfollow, UserContextData context) throws EventException;

	List<User> getFollowingUsers(long userId) throws DbConnectionException;
	
	List<User> getUserFollowers(long userId, Session session);

	List<User> getUserFollowers(long userId);

	boolean isUserFollowingUser(long followerUserId, long followedUserId);

	List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException;
	
	int getNumberOfFollowingUsers(long userId) throws DbConnectionException;

	List<User> getFollowers(long userId) throws DbConnectionException;

}