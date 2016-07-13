/**
 * 
 */
package org.prosolo.services.interaction;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Nikola Milikic
 *
 */
public interface FollowResourceManager extends AbstractManager {

	User followUser(long follower, long userToFollowId, String context) throws EventException, ResourceCouldNotBeLoadedException;
	
	boolean unfollowUser(long followerId, long userToUnfollow, String context) throws EventException;

//	User followResource(User user, Node resourceToFollow, String context) throws EventException;
//	
//	boolean isUserFollowingResource(User followerUser, Node followedResource);
//	
//	FollowedEntity getFollowedEntity(User user, Node followedResource, Session session);
//	
//	Collection<User> getResourceFollowers(BaseEntity resource, Session session);
	
	List<User> getFollowingUsers(long userId) throws DbConnectionException;
	
	List<User> getUserFollowers(long userId, Session session);

	List<User> getUserFollowers(long userId);

//	boolean unfollowResource(User user, Node resourceToUnfollow, String context) throws EventException;

	boolean isUserFollowingUser(long followerUserId, long followedUserId);

	List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException;
	
	int getNumberOfFollowingUsers(long userId) throws DbConnectionException;


}