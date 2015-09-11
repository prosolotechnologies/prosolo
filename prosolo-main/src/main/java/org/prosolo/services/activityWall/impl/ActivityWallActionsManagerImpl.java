/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.activitystream.ActivityWallActionsManager")
public class ActivityWallActionsManagerImpl extends AbstractManagerImpl implements ActivityWallActionsManager {

	private static final long serialVersionUID = -4769526394766538839L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityWallActionsManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;

	@Override
	@Transactional (readOnly = false)
	public SocialActivityConfig hideNotification(long socialActivityId, User user, long configId, String context, 
			Session session) throws ResourceCouldNotBeLoadedException, EventException {
		
		SocialActivityConfig config = null;
		
		if (configId > 0) {
			config = loadResource(SocialActivityConfig.class, configId, session);
		} else {
			config = new SocialActivityConfig();
			
			SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId, session);
			config.setSocialActivity(socialActivity);
			config.setUser(user);
		}
		
		config.setHidden(true);
		config = saveEntity(config, session);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		eventFactory.generateEvent(EventType.HIDE_SOCIAL_ACTIVITY, user, config, parameters);
		
		return config;
	}
	
	@Override
	@Transactional
	public boolean deleteSocialActivity(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException {
		SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId, session);
		
		socialActivity.setDeleted(true);
		session.save(socialActivity);
		session.flush();
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		eventFactory.generateEvent(EventType.Delete, user, socialActivity, parameters);
		return true;
	}

	@Override
	@Transactional
	public boolean enableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException {
		SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId, session);
//		socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
		socialActivity.setCommentsDisabled(false);
		session.save(socialActivity);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		eventFactory.generateEvent(EventType.CommentsEnabled, user, socialActivity, parameters);
		
		return true;
	}

	@Override
	@Transactional
	public boolean disableComments(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException {
		SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId, session);
		socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
		socialActivity.setCommentsDisabled(true);
		socialActivity = saveEntity(socialActivity, session);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		eventFactory.generateEvent(EventType.CommentsDisabled, user, socialActivity, parameters);
		
		return true;
	}
	
	@Override
	@Transactional
	public SocialActivity decrementNumberOfReshares(long socialActivityId, Session session) throws ResourceCouldNotBeLoadedException {
		SocialActivity socialActivity = loadResource(SocialActivity.class, socialActivityId, session);
		
		if (socialActivity != null) {
			socialActivity.setShareCount(socialActivity.getShareCount() - 1);
			session.save(socialActivity);
			
			return socialActivity;
		}
		return null;
	}

}
