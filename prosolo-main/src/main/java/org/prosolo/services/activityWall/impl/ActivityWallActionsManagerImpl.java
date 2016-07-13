/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivityConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.data.LearningContextData;
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
	
	private static Logger logger = Logger.getLogger(ActivityWallActionsManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;

	@Override
	@Transactional (readOnly = false)
	public SocialActivityConfig hideNotification(long socialActivityId, long userId, 
			LearningContextData context, Session session) throws ResourceCouldNotBeLoadedException, EventException {
//		if (configId > 0) {
//			config = loadResource(SocialActivityConfig.class, configId, session);
//		} else {
		User user = (User) session.load(User.class, userId);
		SocialActivityConfig config = new SocialActivityConfig();
		
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		config.setSocialActivity(socialActivity);
		config.setUser(user);
//		}
		
		config.setHidden(true);
		config = saveEntity(config, session);
		String page = context != null ? context.getPage() : null;
		String lContext = context != null ? context.getLearningContext() : null;
		String service = context != null ? context.getService() : null;
		
		eventFactory.generateEvent(EventType.HIDE_SOCIAL_ACTIVITY, user, config, null, page, lContext,
				service, null);
		
		return config;
	}
	
	@Override
	@Transactional
	public boolean deleteSocialActivity(User user, long socialActivityId, String context, Session session) throws EventException, ResourceCouldNotBeLoadedException {
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		
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
	public void deleteSocialActivity(long userId, SocialActivityData1 socialActivity, 
			LearningContextData context, Session session) throws DbConnectionException {
		try {
			SocialActivity1 sa = loadResource(SocialActivity1.class, socialActivity.getId(), session);
			
			sa.setDeleted(true);
			
			if(socialActivity.getType() == SocialActivityType.Post_Reshare) {
				long originalPostSocialActivityId = socialActivity.getObject().getId();
				if(originalPostSocialActivityId > 0) {
					PostSocialActivity1 originalPost = loadResource(PostSocialActivity1.class, 
							originalPostSocialActivityId, session);
					originalPost.setShareCount(originalPost.getShareCount() - 1);
				}
			}
			
			User user = new User();
			user.setId(userId);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.Delete, user, sa, null, page, lContext, service, 
					null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while deleting social activity");
		}
	}

	@Override
	@Transactional
	public void enableComments(long userId, long socialActivityId, LearningContextData context, 
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
	//		socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
			socialActivity.setCommentsDisabled(false);
			
			User user = new User();
			user.setId(userId);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.CommentsEnabled, user, socialActivity, null, page, 
					lContext, service, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while enabling comments");
		}
	}

	@Override
	@Transactional
	public void disableComments(long userId, long socialActivityId, LearningContextData context, 
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
	//		socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
			socialActivity.setCommentsDisabled(true);
			
			User user = new User();
			user.setId(userId);
			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;
			eventFactory.generateEvent(EventType.CommentsDisabled, user, socialActivity, null, page, 
					lContext, service, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while disabling comments");
		}
	}
	
	@Override
	@Transactional
	public SocialActivity1 decrementNumberOfReshares(long socialActivityId, Session session) 
			throws ResourceCouldNotBeLoadedException {
		PostSocialActivity1 socialActivity = loadResource(PostSocialActivity1.class, socialActivityId, 
				session);
		
		if (socialActivity != null) {
			socialActivity.setShareCount(socialActivity.getShareCount() - 1);
			session.save(socialActivity);
			
			return socialActivity;
		}
		return null;
	}

}
