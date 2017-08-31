/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivityConfig;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
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
	
	private static Logger logger = Logger.getLogger(ActivityWallActionsManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;

	@Override
	@Transactional (readOnly = false)
	public SocialActivityConfig hideNotification(long socialActivityId, UserContextData context, Session session) throws ResourceCouldNotBeLoadedException, EventException {
		User user = (User) session.load(User.class, context.getActorId());
		SocialActivityConfig config = new SocialActivityConfig();
		
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		config.setSocialActivity(socialActivity);
		config.setUser(user);
		
		config.setHidden(true);
		config = saveEntity(config, session);
		
		eventFactory.generateEvent(EventType.HIDE_SOCIAL_ACTIVITY, context, config, null, null,null);
		
		return config;
	}
	
	@Override
	@Transactional
	public boolean deleteSocialActivity(User user, long socialActivityId, UserContextData context, Session session) throws EventException, ResourceCouldNotBeLoadedException {
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		
		socialActivity.setDeleted(true);
		session.save(socialActivity);
		session.flush();
		
		eventFactory.generateEvent(EventType.Delete, context, socialActivity, null, null, null);
		return true;
	}
	
	@Override
	@Transactional
	public void deleteSocialActivity(SocialActivityData1 socialActivity,
			UserContextData context, Session session) throws DbConnectionException {
		try {
			SocialActivity1 sa = loadResource(SocialActivity1.class, socialActivity.getId(), session);
			
			sa.setDeleted(true);

			eventFactory.generateEvent(EventType.Delete, context, sa, null, null, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while deleting social activity");
		}
	}

	@Override
	@Transactional
	public void enableComments(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
			socialActivity.setCommentsDisabled(false);

			eventFactory.generateEvent(EventType.CommentsEnabled, context, socialActivity, null, null, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while enabling comments");
		}
	}

	@Override
	@Transactional
	public void disableComments(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
			socialActivity.setCommentsDisabled(true);

			eventFactory.generateEvent(EventType.CommentsDisabled, context, socialActivity, null, null, null);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while disabling comments");
		}
	}
	
}
