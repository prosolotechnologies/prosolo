package org.prosolo.services.interaction.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 
 * @author zoran jeremic
 *
 */
@Service("org.prosolo.services.interaction.FollowResourceAsyncManager")
public class FollowResourceAsyncManagerImpl implements FollowResourceAsyncManager, Serializable {

	private static final long serialVersionUID = -8782380873248039230L;
	
	private static Logger logger = Logger.getLogger(FollowResourceAsyncManagerImpl.class);

	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private FollowResourceManager followManager;

	@Override
	@Transactional
	public boolean asyncFollowUser(final User userToFollow, final UserContextData context) {
		taskExecutor.execute(() -> {
			try {
				followManager.followUser(userToFollow.getId(), context);
			} catch (Exception e) {
				logger.error(e);
			}
        });
		return true;
	}
	
	@Override
//	@Transactional
	public boolean asyncUnfollowUser(final User userToUnfollow, final UserContextData context) {
		taskExecutor.execute(() -> {
			followManager.unfollowUser(userToUnfollow.getId(), context);
        });
		return true;
	}
//	@Override
//	@Transactional
//	public boolean asyncFollowResource(final User follower, final  Node resourceToFollow, 
//			final String context) {
//		taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//            	try {
//					followManager.followResource(follower, HibernateUtil.initializeAndUnproxy(resourceToFollow), context);
//				} catch (EventException e) {
//					logger.error(e);;
//				}
//            }
//        });
//		return true;
//	}
//	
//	@Override
//	@Transactional
//	public boolean asyncUnfollowResource(final User user, final Node resourceToUnfollow, final String context) {
//		taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//            	try {
//					followManager.unfollowResource(user,  HibernateUtil.initializeAndUnproxy(resourceToUnfollow), context);
//				} catch (EventException e) {
//					logger.error(e);
//				}
//            }
//        });
//		return true;
//	}
}
