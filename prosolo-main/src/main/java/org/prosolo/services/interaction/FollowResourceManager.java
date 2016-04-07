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
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Nikola Milikic
 *
 */
public interface FollowResourceManager extends AbstractManager {

	User followUser(User follower, User userToFollow, String context) throws EventException;

	User followResource(User user, Node resourceToFollow, String context) throws EventException;
	
	boolean isUserFollowingResource(User followerUser, Node followedResource);
	
	FollowedEntity getFollowedEntity(User user, Node followedResource, Session session);
	
	Collection<User> getResourceFollowers(BaseEntity resource, Session session);
	
	List<User> getFollowingUsers(User user);
	
	List<User> getUserFollowers(User user, Session session);

	List<User> getUserFollowers(User user);
	
	List<User> getUserFollowers(long userId, Session session);

	List<User> getUserFollowers(long userId);

	boolean unfollowUser(User user, User userToUnfollow, String context) throws EventException;

	boolean unfollowResource(User user, Node resourceToUnfollow, String context) throws EventException;

	boolean isUserFollowingUser(User followerUser, User followedUser);


}