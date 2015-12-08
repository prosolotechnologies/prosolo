package org.prosolo.services.notifications;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater;
import org.prosolo.services.event.CentralEventDispatcher;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessor;
import org.prosolo.services.notifications.eventprocessing.NotificationEventProcessorFactory;
import org.prosolo.web.ApplicationBean;
import org.springframework.stereotype.Service;

/**
 * This class is an observer to the {@link CentralEventDispatcher} that is invoked whenever an event that is related to a notification occurs.  
 */
@Service("org.prosolo.services.notifications.NotificationObserver")
public class NotificationObserver implements EventObserver {

	private static Logger logger = Logger.getLogger(NotificationObserver.class.getName());

	@Inject private ApplicationBean applicationBean;
	@Inject private DefaultManager defaultManager;
	@Inject private NotificationCacheUpdater notificationCacheUpdater;
	@Inject private SessionMessageDistributer messageDistributer;
	@Inject private NotificationEventProcessorFactory notificationEventProcessorFactory;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.JOIN_GOAL_REQUEST,
				EventType.JOIN_GOAL_REQUEST_APPROVED,
				EventType.JOIN_GOAL_REQUEST_DENIED,
				EventType.JOIN_GOAL_INVITATION,
				EventType.JOIN_GOAL_INVITATION_ACCEPTED,
				EventType.EVALUATION_REQUEST, 
				EventType.EVALUATION_ACCEPTED,
				EventType.EVALUATION_GIVEN, 
//				EventType.EVALUATION_EDITED, 
				EventType.Follow,
				EventType.ACTIVITY_REPORT_AVAILABLE,
				EventType.Comment,
				EventType.Like,
				EventType.Dislike,
				EventType.Post,
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		try {
			NotificationEventProcessor processor = notificationEventProcessorFactory
					.getNotificationEventProcessor(event, session);
			List<Notification> notifications = processor.getNotificationList();
			// make sure all data is persisted to the database
			session.flush();
			
			
			/*
			 * After all notifications have been generated, them to their
			 * receivers. If those users are logged in, their notification cache
			 * will be updated with these new notifications.
			 */
			if (!notifications.isEmpty()) {
				
				for (Notification notification : notifications) {
					if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
						messageDistributer.distributeMessage(
								ServiceType.ADD_NOTIFICATION, 
								notification.getReceiver().getId(),
								notification.getId(), 
								null, 
								null);
					} else {
						HttpSession httpSession = applicationBean.getUserSession(notification.getReceiver().getId());
						
						notificationCacheUpdater.updateNotificationData(
								notification.getId(), 
								httpSession, 
								session);
					}
				}
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		} finally {
			HibernateUtil.close(session);
		}
	}

}
