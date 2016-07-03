package org.prosolo.services.notifications.eventprocessing;

import javax.inject.Inject;

import org.hibernate.Session;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventProcessorFactory {

	@Inject
	private NotificationManager notificationManager;
	@Inject
	private NotificationsSettingsManager notificationsSettingsManager;
	@Inject
	private Activity1Manager activityManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CommentManager commentManager;
	@Autowired 
	private FollowResourceManager followResourceManager;
	@Inject
	private RoleManager roleManager;

	public NotificationEventProcessor getNotificationEventProcessor(Event event, Session session) {
		switch (event.getAction()) {
		/*
		 * A new comment was posted. If comment is posted on competence/activity
		 * page notify user that created competence/activity and all users that
		 * commented on that competence/activity.
		 */
		case Comment:
		case Comment_Reply:
			return new CommentEventProcessing(event, session, notificationManager,
					notificationsSettingsManager, idEncoder, activityManager, commentManager, roleManager);
		/*
		 * Someone liked or disliked a resource. We need to determine whether it
		 * was generated on the Status Wall (liked/disliked a SocialActivity
		 * instance). Or the comment was created on the Activity Wall
		 * (liked/disliked a TargetActivity instance)
		 */
		case Like:
		case Dislike:
			return new LikeEventProcessing(event, session, notificationManager, notificationsSettingsManager,
					activityManager, idEncoder);
		case Follow:
			return new FollowUserEventProcessor(event, session, notificationManager, notificationsSettingsManager,
					idEncoder, followResourceManager);
		case AssessmentApproved:
			return new AssessmentApprovedEventProcessor(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		case AssessmentRequested:
			return new AssessmentRequestEventProcessor(event, session, notificationManager, notificationsSettingsManager, idEncoder);
			
		default:
			return null;
		}
	}
}
