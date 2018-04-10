package org.prosolo.services.notifications.eventprocessing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

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
	private AssessmentManager assessmentManager;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private ContextJsonParserService contextJsonParserService;

	public NotificationEventProcessor getNotificationEventProcessor(Event event, Session session) {
		switch (event.getAction()) {
		/*
		 * A new comment was posted. If comment is posted on competence/activity
		 * page notify user that created competence/activity and all users that
		 * commented on that competence/activity.
		 */
		case Comment:
		case Comment_Reply:
			return new CommentPostEventProcessor(event, session, notificationManager,
					notificationsSettingsManager, activityManager, idEncoder, commentManager,
					contextJsonParserService);
		/*
		 * Someone liked or disliked a resource. We need to determine whether it
		 * was generated on the Status Wall (liked/disliked a SocialActivity
		 * instance). Or the comment was created on the Activity Wall
		 * (liked/disliked a TargetActivity instance)
		 */
		case Like:
		case Dislike:
			if (event.getObject() instanceof Comment1) {
				return new CommentLikeEventProcessor(event, session, notificationManager, 
						notificationsSettingsManager, activityManager, idEncoder,
						contextJsonParserService);
			} else if (event.getObject() instanceof SocialActivity1) {
				return new SocialActivityLikeEventProcessor(event, session, notificationManager, 
						notificationsSettingsManager, activityManager, idEncoder);
			}
		case Follow:
			return new FollowUserEventProcessor(event, session, notificationManager, 
					notificationsSettingsManager, idEncoder, followResourceManager);
		case AssessmentComment:
			BaseEntity target = event.getTarget();
			if (target instanceof ActivityAssessment) {
				return new ActivityAssessmentCommentEventProcessor(event, session, notificationManager,
						notificationsSettingsManager, idEncoder, assessmentManager, contextJsonParserService);
			} else if (target instanceof CompetenceAssessment) {
				return new CompetenceAssessmentCommentEventProcessor(event, session, notificationManager,
						notificationsSettingsManager, idEncoder, assessmentManager, contextJsonParserService);
			} else if (target instanceof CredentialAssessment) {
				return new CredentialAssessmentCommentEventProcessor(event, session, notificationManager,
						notificationsSettingsManager, idEncoder, assessmentManager);
			}
		case AssessmentApproved:
			return new AssessmentApprovedEventProcessor(event, session, notificationManager, 
					notificationsSettingsManager, idEncoder, contextJsonParserService);
		case AssessmentRequested:
			if (event.getObject() instanceof CredentialAssessment) {
				return new CredentialAssessmentRequestEventProcessor(event, session, notificationManager,
						notificationsSettingsManager, idEncoder);
			} else if (event.getObject() instanceof CompetenceAssessment) {
				return new CompetenceAssessmentRequestEventProcessor(event, session, notificationManager,
						notificationsSettingsManager, idEncoder, contextJsonParserService, assessmentManager);
			}
		case AnnouncementPublished:
			return new AnnouncementPublishedEventProcessor(event, session, notificationManager, 
					notificationsSettingsManager, idEncoder, credentialManager);
		case GRADE_ADDED:
			return new GradeAddedEventProcessor(event, session, notificationManager,
					notificationsSettingsManager, idEncoder, contextJsonParserService, assessmentManager);
		default:
			return null;
		}
	}
}
