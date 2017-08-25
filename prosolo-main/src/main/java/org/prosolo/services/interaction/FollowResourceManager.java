/**
 * 
 */
package org.prosolo.services.interaction;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.common.exception.EntityAlreadyExistsException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;

import java.util.List;

/**
 * @author Nikola Milikic
 *
 */
public interface FollowResourceManager extends AbstractManager {

	User followUser(long userToFollowId, UserContextData learningContext) throws DbConnectionException, EntityAlreadyExistsException;
	
	boolean unfollowUser(long userToUnfollow, UserContextData context) throws EventException;

	List<User> getFollowingUsers(long userId) throws DbConnectionException;
	
	List<User> getUserFollowers(long userId, Session session);

	List<User> getUserFollowers(long userId);

	boolean isUserFollowingUser(long followerUserId, long followedUserId);

	List<User> getFollowingUsers(long userId, int page, int limit) throws DbConnectionException;
	
	int getNumberOfFollowingUsers(long userId) throws DbConnectionException;

	List<User> getFollowers(long userId) throws DbConnectionException;

}