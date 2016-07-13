package org.prosolo.services.interaction;

import org.prosolo.common.domainmodel.user.User;

public interface FollowResourceAsyncManager {

	boolean asyncFollowUser(long followerId, User userToFollow, String context);

	boolean asyncUnfollowUser(long followerId, User userToUnfollow, String context);

//	boolean asyncFollowResource(User follower, Node resourceToFollow, String context);
//
//	boolean asyncUnfollowResource(User user, Node resourceToUnfollow, String context);

}
