package org.prosolo.services.notifications.eventprocessing;

import javax.inject.Inject;

import org.hibernate.Session;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationManager;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventProcessorFactory {
	
	@Inject
	private NotificationManager notificationManager;
	@Inject
	private DefaultManager defaultManager;
	@Inject
	private NotificationsSettingsManager notificationsSettingsManager;

	public NotificationEventProcessor getNotificationEventProcessor(Event event, Session session) {
		switch(event.getAction()) {
			/*
			 *  Invoked when someone has requested to join user's learning goal.
			 *  Goal maker should be notified about this.
			 */
			case JOIN_GOAL_REQUEST:
			/*
			 * Someone has invited other user to join his learning goal.
			 * User being invited should be notified.
			 */
			case JOIN_GOAL_INVITATION:
			/*
			 * A user has requested from another user to give him/her an assessment (evaluation)
			 * for a resource. The assessor shoul be notified about this request.
			 */
			case EVALUATION_REQUEST:
				return new RequestWithCommentEventProcessor(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 *  Invoked when join goal request has been denied.
			 *  Requester should be notified about this
			 */
			case JOIN_GOAL_REQUEST_DENIED:
			/*
			 * Someone has invited other user to join his learning goal and the
			 * other user has accepted the invitation. User that invited the other 
			 * user should be notified.
			 */
			case JOIN_GOAL_INVITATION_ACCEPTED:
			/*
			 * Following receiving a request for an assessment, the assessor has accepted it.
			 * The assessment requester should be notified about this.
			 */
				/*
				 *  Invoked when join goal request has been approved.
				 *  Requester should be notified about this
				 */
			case JOIN_GOAL_REQUEST_APPROVED:
			case EVALUATION_ACCEPTED:
				return new RequestWithoutCommentEventProcessor(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 * A new assessment has been submitted. USer being assessed should be 
			 * notified about this.
			 */
			case EVALUATION_GIVEN:
				return new EvaluationGivenProcessor(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 * Someone started following a user. User being followed should 
			 * be notified about this.
			 */
			case Follow:
				return new FollowEventProcessor(event, session, 
						notificationManager, notificationsSettingsManager, 
						defaultManager);
			/*
			 * Weekly activity report has been generated and is available for download
			 * from the Settings -> Reports page. User for whom the report has been
			 * created shuold be notified about this.
			 */
			case ACTIVITY_REPORT_AVAILABLE:
				return new ActivityReportAvailableEventProcessor(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 * A new comment was posted. We need to determine whether it was generated
			 * on the Status Wall (commented on a SocialActivity instance). Or the comment
			 * was created on the Activity Wall (commented on a TargetActivity instance)
			 */
			case Comment:
				return new CommentEventProcessing(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 * Someone liked or disliked a resource. We need to determine whether it was generated
			 * on the Status Wall (liked/disliked a SocialActivity instance). Or the comment
			 * was created on the Activity Wall (liked/disliked a TargetActivity instance)
			 */
			case Like:
			case Dislike:
				return new LikeEventProcessing(event, session, 
						notificationManager, notificationsSettingsManager);
			/*
			 * A new post has been created. If some users are mentioned in it,
			 * they should be notified.
			 */
			case Post:
				return new PostEventProcessor(event, session, 
						notificationManager, notificationsSettingsManager);
			default:
				return null;
		}
	}
}
