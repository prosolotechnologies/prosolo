package org.prosolo.services.interaction.impl;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public boolean asyncFollowUser(final User follower, final User userToFollow, final String context) {
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
	           	try {
					followManager.followUser(follower, HibernateUtil.initializeAndUnproxy(userToFollow), context);
				} catch (EventException e) {
					logger.error(e);
				}
            }
        });
		return true;
	}
	
	@Override
//	@Transactional
	public boolean asyncUnfollowUser(final User user, final User userToUnfollow, final String context) {
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	try {
					followManager.unfollowUser(user, HibernateUtil.initializeAndUnproxy(userToUnfollow), context);
				} catch (EventException e) {
					logger.error(e);
				}	 
            }
        });
		return true;
	}
	@Override
	@Transactional
	public boolean asyncFollowResource(final User follower, final  Node resourceToFollow, 
			final String context) {
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	try {
					followManager.followResource(follower, HibernateUtil.initializeAndUnproxy(resourceToFollow), context);
				} catch (EventException e) {
					logger.error(e);;
				}
            }
        });
		return true;
	}
	
	@Override
	@Transactional
	public boolean asyncUnfollowResource(final User user, final Node resourceToUnfollow, final String context) {
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	try {
					followManager.unfollowResource(user,  HibernateUtil.initializeAndUnproxy(resourceToUnfollow), context);
				} catch (EventException e) {
					logger.error(e);
				}
            }
        });
		return true;
	}
}
