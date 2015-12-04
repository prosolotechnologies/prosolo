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
import org.prosolo.web.ApplicationBean;
import org.springframework.stereotype.Service;

/**
 * This class is an observer to the {@link CentralEventDispatcher} that is invoked whenever an event that is related to a notification occurs.  
 */
@Service("org.prosolo.services.notifications.NotificationObserver")
public class NotificationObserver implements EventObserver {

	private static Logger logger = Logger.getLogger(NotificationObserver.class.getName());

	@Inject private NotificationManager notificationsManager;
	@Inject private ApplicationBean applicationBean;
	@Inject private DefaultManager defaultManager;
	@Inject private EvaluationUpdater evaluationUpdater;
	@Inject private NotificationCacheUpdater notificationCacheUpdater;
	@Inject private SessionMessageDistributer messageDistributer;
	@Inject private LearningGoalRecommendationCacheUpdater learningGoalRecommendationCacheUpdater;

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
			List<Notification> notifications = new ArrayList<Notification>();
			EventType action = event.getAction();
			BaseEntity object = event.getObject();
			User actor = event.getActor();
			
			
			if (action.equals(EventType.JOIN_GOAL_REQUEST)) {
				/*
				 *  Invoked when someone has requested to join user's learning goal.
				 *  Goal maker should be notified about this.
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getSentTo(), 
						event.getAction(), 
						request.getComment(),
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.JOIN_GOAL_REQUEST_APPROVED)) {
				/*
				 *  Invoked when join goal request has been approved.
				 *  Requester should be notified about this
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getMaker(), 
						event.getAction(), 
						null,					// no message is provided
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
				
				checkRecommendedationsForAcceptedLearningGoal(
						(TargetLearningGoal) request.getResource(), 
						request.getMaker(), 
						session);
			} else if (action.equals(EventType.JOIN_GOAL_REQUEST_DENIED)) {
				/*
				 *  Invoked when join goal request has been denied.
				 *  Requester should be notified about this
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getMaker(), 
						event.getAction(), 
						null,					// no message is provided
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.JOIN_GOAL_INVITATION)) {
				/*
				 * Someone has invited other user to join his learning goal.
				 * User being invited should be notified.
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getSentTo(), 
						event.getAction(), 
						request.getComment(),
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED)) {
				/*
				 * Someone has invited other user to join his learning goal and the
				 * other user has accepted the invitation. User that invited the other 
				 * user should be notified.
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getMaker(), 
						event.getAction(), 
						null,					// no message is provided
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.EVALUATION_REQUEST)) {
				/*
				 * A user has requested from another user to give him/her an assessment (evaluation)
				 * for a resource. The assessor shoul be notified about this request.
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getSentTo(), 
						event.getAction(), 
						request.getComment(),
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.EVALUATION_ACCEPTED)) {
				/*
				 * Following receiving a request for an assessment, the assessor has accepted it.
				 * The assessment requester should be notified about this.
				 */
				Request request = (Request) session.merge(object);
	
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						request.getMaker(), 
						event.getAction(), 
						null,					// no message is provided
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.EVALUATION_GIVEN)) {
				/*
				 * A new assessment has been submitted. USer being assessed should be 
				 * notified about this.
				 */
				EvaluationSubmission evSubmission = (EvaluationSubmission) object;	
				User receiver = evSubmission.getRequest().getMaker();
	
				Notification notification = notificationsManager.createNotification(
						evSubmission, 
						evSubmission.getMaker(), 
						receiver, 
						event.getAction(),
						evSubmission.getMessage(), 
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
	
				/*
				 * If user assessed is online, update the number of assessments for this resource.
				 * This number of assessments is being updated in session scoped beans used for displaying
				 * number of evaluations on Learn and Profile pages.
				 */
				long receiverId = receiver.getId();
				
				if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
					messageDistributer.distributeMessage(
						ServiceType.UPDATE_EVALUAIION_DATA,
						receiverId, 
						evSubmission.getId(), 
						null, 
						null);
				} else {
					HttpSession userSession = applicationBean.getUserSession(receiverId);
					
					try {
						evaluationUpdater.updateEvaluationData(
							evSubmission.getId(),
							userSession,
							session);
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error(e);
					}
				}
			} else if (action.equals(EventType.Follow)) {
				/*
				 * Someone started following a user. User being followed should 
				 * be notified about this.
				 */
				try {
					User follower = defaultManager.loadResource(User.class, actor.getId(), session);
					User userToFollow = defaultManager.loadResource(User.class, event.getObject().getId(), session);
					
					Notification notification = notificationsManager.createNotification(
							null, 
							follower, 
							userToFollow,
							event.getAction(), 
							null,
							event.getDateCreated(), 
							session);
					
					notifications.add(notification);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			} else if (action.equals(EventType.ACTIVITY_REPORT_AVAILABLE)) {
				/*
				 * Weekly activity report has been generated and is available for download
				 * from the Settings -> Reports page. User for whom the report has been
				 * created shuold be notified about this.
				 */
				Notification notification = notificationsManager.createNotification(
						null, 
						null, 
						actor, 
						event.getAction(), 
						null, 
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (action.equals(EventType.Comment)) {
				/*
				 * A new comment was posted. We need to determine whether it was generated
				 * on the Status Wall (commented on a SocialActivity instance). Or the comment
				 * was created on the Activity Wall (commented on a TargetActivity instance)
				 */
				Comment comment = (Comment) session.merge(object);
				BaseEntity commentedResource = comment.getObject();
				User receiver = null;
				
				if (commentedResource instanceof SocialActivity) {
					receiver = ((SocialActivity) comment.getObject()).getMaker();
					
				} else if (commentedResource instanceof TargetActivity) {
					receiver = ((TargetActivity) comment.getObject()).getMaker();
				}
				
				if (receiver != null) {
					if (actor.getId() != receiver.getId()) {
						Notification notification = notificationsManager.createNotification(
								comment,
								actor, 
								receiver, 
								event.getAction(), 
								comment.getText(),
								event.getDateCreated(), 
								session);
						
						notifications.add(notification);
					}
				} else {
					logger.error("Commenting on the resource of a type: " + comment.getObject().getClass() + " is not captured.");
				}
			} else if (action.equals(EventType.Like) || action.equals(EventType.Dislike)) {
				/*
				 * Someone liked or disliked a resource. We need to determine whether it was generated
				 * on the Status Wall (liked/disliked a SocialActivity instance). Or the comment
				 * was created on the Activity Wall (liked/disliked a TargetActivity instance)
				 */
				User receiver = null;
				
				if (object instanceof SocialActivity) {
					receiver = ((SocialActivity) object).getMaker();
					
				} else if (object instanceof TargetActivity) {
					receiver = ((TargetActivity) object).getMaker();
				}
				
				if (receiver != null) {
					if (actor.getId() != receiver.getId()) {
						Notification notification = notificationsManager.createNotification(
							object,
							actor, 
							receiver, 
							event.getAction(), 
							null,
							event.getDateCreated(), 
							session);
						
						notifications.add(notification);
					}
				} else {
					logger.error("Commenting on the resource of a type: " + object.getClass() + " is not captured.");
				}
			} else if (action.equals(EventType.Post)) {
				/*
				 * A new post has been created. If some users are mentioned in it,
				 * they should be notified.
				 */
				Post post = (Post) object;
				
				if (post.getMentionedUsers() != null) {
					for (User user : post.getMentionedUsers()) {
						Notification notification = notificationsManager.createNotification(
							post,
							actor, 
							user, 
							EventType.MENTIONED, 
							null,
							event.getDateCreated(), 
							session);
						
						notifications.add(notification);
					}
				}
			} 
			
			
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

	public void checkRecommendedationsForAcceptedLearningGoal(TargetLearningGoal targetGoal, User receiver, Session session) {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			messageDistributer.distributeMessage(
					ServiceType.JOINED_LEARNING_GOAL,
					receiver.getId(), 
					targetGoal.getLearningGoal().getId(), 
					null, 
					null);
		} else {
			HttpSession userSession = applicationBean.getUserSession(receiver.getId());

			learningGoalRecommendationCacheUpdater.removeLearningGoalRecommendation(
					receiver.getId(), 
					targetGoal.getLearningGoal().getId(), 
					userSession, 
					session);
		}
	}

}
