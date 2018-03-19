package org.prosolo.services.interaction;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;

public interface FollowResourceAsyncManager {

	boolean asyncFollowUser(User userToFollow, UserContextData context);

	boolean asyncUnfollowUser(User userToUnfollow, UserContextData context);

//	boolean asyncFollowResource(User follower, Node resourceToFollow, String context);
//
//	boolean asyncUnfollowResource(User user, Node resourceToUnfollow, String context);

}
