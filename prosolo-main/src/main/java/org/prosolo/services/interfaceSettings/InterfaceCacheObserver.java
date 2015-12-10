package org.prosolo.services.interfaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interfaceSettings.eventProcessors.InterfaceEventProcessorFactory;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.portfolio.PortfolioBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.interfaceSettings.InterfaceCacheUpdater")
public class InterfaceCacheObserver implements EventObserver {
	
	private static Logger logger = Logger.getLogger(InterfaceCacheObserver.class);
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private ActivityManager activityManager;
	@Autowired private CommentUpdater commentUpdater;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private LearnPageCacheUpdater learnPageCacheUpdater;
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Inject private InterfaceEventProcessorFactory interfaceEventProcessorFactory;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.Delete,
			EventType.Detach,
			EventType.CommentsEnabled,
			EventType.CommentsDisabled,
			EventType.Like,
			EventType.RemoveLike,
			EventType.Dislike,
			EventType.RemoveDislike,
			EventType.Comment,
			EventType.PostShare,
			EventType.PostUpdate,
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			SocialActivity.class,
			Comment.class,
			LearningGoal.class,
			Post.class
		};
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) goalManager.getPersistence().openSession();
		
		try {
			BaseEntity target = event.getTarget();
			
			if (target != null) {
				target = HibernateUtil.initializeAndUnproxy(target);
			}
			
			interfaceEventProcessorFactory.getInterfaceEventProcessor(session, event).processEvent();

		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

	public void asyncResetGoalCollaborators(final long goalId, final User user) { 
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
		    	Session session = (Session) goalManager.getPersistence().openSession();
		    	
		    	try {
			    	List<User> collaborators = goalManager.retrieveCollaborators(goalId, user, session);
			    	
			    	for (User user : collaborators) {
				    	HttpSession userSession = applicationBean.getUserSession(user.getId());
			    		
						if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(
									ServiceType.ACCEPT_JOIN_GOAL_NOTIFICATION,
									user.getId(), 
									goalId, 
									null, 
									null);
						} else if (userSession != null) {
			    			LearnBean userLearningGoalBean = (LearnBean) userSession.getAttribute("learninggoals");
							
							if (userLearningGoalBean != null) {
								GoalDataCache goalData = userLearningGoalBean.getData().getDataForGoal(goalId);
								
								if (goalData != null) {
									goalData.setCollaborators(null);
									goalData.initializeCollaborators();
								}
							}
						} 
			    	}
			    } catch (Exception e) {
					logger.error(e);
				} finally {
					HibernateUtil.close(session);
				}
		    }
		});
	}

}
