/**
 * 
 */
package org.prosolo.services.interaction;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.following.FollowedEntity;
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

	User followResource(User user, Node resourceToFollow, String context) throws EventException;
	
	boolean isUserFollowingResource(User followerUser, Node followedResource);
	
	FollowedEntity getFollowedEntity(User user, Node followedResource, Session session);
	
	Collection<User> getResourceFollowers(BaseEntity resource, Session session);
	
	List<User> getFollowingUsers(long userId) throws DbConnectionException;
	
	List<User> getUserFollowers(User user, Session session);

	List<User> getUserFollowers(User user);
	
	List<User> getUserFollowers(long userId, Session session);

	List<User> getUserFollowers(long userId);

	boolean unfollowUser(User user, User userToUnfollow, String context) throws EventException;

	boolean unfollowResource(User user, Node resourceToUnfollow, String context) throws EventException;

	boolean isUserFollowingUser(long followerUserId, long followedUserId);

	List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException;
	
	int getNumberOfFollowingUsers(long userId) throws DbConnectionException;


}