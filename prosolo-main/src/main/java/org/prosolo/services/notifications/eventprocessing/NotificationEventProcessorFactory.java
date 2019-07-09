package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.ActivityAssessment;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.event.Event;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.user.StudentProfileManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class NotificationEventProcessorFactory {

    @Inject private NotificationManager notificationManager;
    @Inject private NotificationsSettingsManager notificationsSettingsManager;
    @Inject private Activity1Manager activityManager;
    @Inject private UrlIdEncoder idEncoder;
    @Inject private CommentManager commentManager;
    @Inject private AssessmentManager assessmentManager;
    @Inject private CredentialManager credentialManager;
    @Inject private StudentProfileManager studentProfileManager;
    @Inject private SocialActivityManager socialActivityManager;
    @Inject private AnnouncementManager announcementManager;

    public NotificationEventProcessor getNotificationEventProcessor(Event event) throws ResourceCouldNotBeLoadedException {
        switch (event.getAction()) {
            case Follow:
                return new FollowUserEventProcessor(event, notificationManager,
                        notificationsSettingsManager, idEncoder, studentProfileManager);
            /*
             * A new comment was posted. If comment is posted on competence/activity
             * page notify user that created competence/activity and all users that
             * commented on that competence/activity.
             */
            case Comment:
            case Comment_Reply:
                return new CommentPostEventProcessor(event, notificationManager,
                        notificationsSettingsManager, activityManager, idEncoder, commentManager, socialActivityManager,
                        assessmentManager);
            /*
             * Someone liked or disliked a resource. We need to determine whether it
             * was generated on the Status Wall (liked/disliked a SocialActivity
             * instance). Or the comment was created on the Activity Wall
             * (liked/disliked a TargetActivity instance)
             */
            case Like:
            case Dislike:
                if (event.getObject() instanceof Comment1) {
                    return new CommentLikeEventProcessor(event, notificationManager,
                            notificationsSettingsManager, activityManager, idEncoder, commentManager);
                } else if (event.getObject() instanceof SocialActivity1) {
                    return new SocialActivityLikeEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, socialActivityManager);
                }
                break;
            case AssessmentComment:
                BaseEntity target = event.getTarget();
                if (target instanceof ActivityAssessment) {
                    return new ActivityAssessmentCommentEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                } else if (target instanceof CompetenceAssessment) {
                    return new CompetenceAssessmentCommentEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                } else if (target instanceof CredentialAssessment) {
                    return new CredentialAssessmentCommentEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                }
                break;
            case AssessmentApproved:
                if (event.getObject() instanceof CredentialAssessment) {
                    return new CredentialAssessmentApprovedEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                } else if (event.getObject() instanceof CompetenceAssessment) {
                    return new CompetenceAssessmentApprovedEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                }
                break;
            case AssessmentRequested:
                if (event.getObject() instanceof CredentialAssessment) {
                    return new CredentialAssessmentRequestEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                } else if (event.getObject() instanceof CompetenceAssessment) {
                    return new CompetenceAssessmentRequestEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                }
                break;
            case AnnouncementPublished:
                return new AnnouncementPublishedEventProcessor(event, notificationManager,
                        notificationsSettingsManager, idEncoder, credentialManager, announcementManager);
            case GRADE_ADDED:
                BaseEntity assessment = event.getObject();
                if (assessment instanceof ActivityAssessment) {
                    return new ActivityGradeAddedEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager, activityManager);
                } else if (assessment instanceof CompetenceAssessment) {
                    return new CompetenceGradeAddedEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                } else if (assessment instanceof CredentialAssessment) {
                    return new CredentialGradeAddedEventProcessor(event, notificationManager,
                            notificationsSettingsManager, idEncoder, assessmentManager);
                }
                break;
            case ASSESSMENT_REQUEST_ACCEPTED:
                return new CompetenceAssessmentRequestAcceptEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
            case ASSESSMENT_REQUEST_DECLINED:
                return new CompetenceAssessmentRequestDeclineEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
            case ASSESSOR_WITHDREW_FROM_ASSESSMENT:
                return new CompetenceAssessmentWithdrawEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
            case ASSESSOR_ASSIGNED_TO_ASSESSMENT:
                return new AssessorAssignedToExistingCompetenceAssessmenEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
            case ASSESSMENT_REQUEST_EXPIRED:
                return new CompetenceAssessmentRequestExpiredNotificationEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
            case ASSESSMENT_TOKENS_NUMBER_UPDATED:
                return new AssessmentTokensUpdatedEventProcessor(event, notificationManager, notificationsSettingsManager, idEncoder);
            default:
                return null;
        }
        return null;
    }
}
