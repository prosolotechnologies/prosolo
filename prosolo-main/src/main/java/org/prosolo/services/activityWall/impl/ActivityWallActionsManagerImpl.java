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
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.ActivityWallActionsManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.activitystream.ActivityWallActionsManager")
public class ActivityWallActionsManagerImpl extends AbstractManagerImpl implements ActivityWallActionsManager {

	private static final long serialVersionUID = -4769526394766538839L;
	
	private static Logger logger = Logger.getLogger(ActivityWallActionsManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	@Inject private ActivityWallActionsManager self;

	@Override
	public SocialActivityConfig hideNotification(long socialActivityId, UserContextData context, Session session) throws ResourceCouldNotBeLoadedException {
		Result<SocialActivityConfig> res = self.hideNotificationAndGetEvents(socialActivityId, context, session);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional (readOnly = false)
	public Result<SocialActivityConfig> hideNotificationAndGetEvents(long socialActivityId, UserContextData context, Session session) throws ResourceCouldNotBeLoadedException {
		User user = (User) session.load(User.class, context.getActorId());
		SocialActivityConfig config = new SocialActivityConfig();
		
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		config.setSocialActivity(socialActivity);
		config.setUser(user);
		
		config.setHidden(true);
		config = saveEntity(config, session);

		Result<SocialActivityConfig> res = new Result<>();
		res.appendEvent(eventFactory.generateEventData(EventType.HIDE_SOCIAL_ACTIVITY, context, config, null, null,null));
		res.setResult(config);
		return res;
	}

	@Override
	public boolean deleteSocialActivity(User user, long socialActivityId, UserContextData context, Session session) throws ResourceCouldNotBeLoadedException {
		Result<Boolean> res = self.deleteSocialActivityAndGetEvents(user, socialActivityId, context, session);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
		return res.getResult();
	}
	
	@Override
	@Transactional
	public Result<Boolean> deleteSocialActivityAndGetEvents(User user, long socialActivityId, UserContextData context, Session session) throws ResourceCouldNotBeLoadedException {
		SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
		
		socialActivity.setDeleted(true);
		session.save(socialActivity);
		session.flush();

		Result<Boolean> res = new Result<>();
		res.appendEvent(eventFactory.generateEventData(EventType.Delete, context, socialActivity, null, null, null));
		res.setResult(true);
		return res;
	}

	@Override
	public void deleteSocialActivity(SocialActivityData1 socialActivity,
									 UserContextData context, Session session) throws DbConnectionException {
		Result<Void> res = self.deleteSocialActivityAndGetEvents(socialActivity, context, session);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional
	public Result<Void> deleteSocialActivityAndGetEvents(SocialActivityData1 socialActivity,
														 UserContextData context, Session session) throws DbConnectionException {
		try {
			SocialActivity1 sa = loadResource(SocialActivity1.class, socialActivity.getId(), session);
			
			sa.setDeleted(true);

			return Result.of(
					EventQueue.of(
							eventFactory.generateEventData(
									EventType.Delete,
									context,
									sa,
									null,
									null,
									null)));
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error deleting social activity");
		}
	}

	@Override
	public void enableComments(long socialActivityId, UserContextData context,
							   Session session) throws DbConnectionException {
		Result<Void> res = self.enableCommentsAndGetEvents(socialActivityId, context, session);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> enableCommentsAndGetEvents(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
			socialActivity.setCommentsDisabled(false);

			return Result.of(
					EventQueue.of(
							eventFactory.generateEventData(
									EventType.CommentsEnabled,
									context,
									socialActivity,
									null,
									null,
									null)));
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error enabling comments");
		}
	}

	@Override
	public void disableComments(long socialActivityId, UserContextData context,
								Session session) throws DbConnectionException {
		Result<Void> res = self.disableCommentsAndGetEvents(socialActivityId, context, session);
		eventFactory.generateAndPublishEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> disableCommentsAndGetEvents(long socialActivityId, UserContextData context,
			Session session) throws DbConnectionException {
		try {
			SocialActivity1 socialActivity = loadResource(SocialActivity1.class, socialActivityId, session);
			socialActivity.setCommentsDisabled(true);

			return Result.of(
					EventQueue.of(
							eventFactory.generateEventData(
									EventType.CommentsDisabled,
									context,
									socialActivity,
									null,
									null,
									null)));
		} catch(Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error disabling comments");
		}
	}
	
}
