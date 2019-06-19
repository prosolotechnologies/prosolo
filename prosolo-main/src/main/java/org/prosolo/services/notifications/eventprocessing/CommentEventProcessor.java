package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.context.Context;
import org.prosolo.common.event.context.ContextName;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;
import java.util.Optional;

public abstract class CommentEventProcessor extends SimpleNotificationEventProcessor {

    private static Logger logger = Logger.getLogger(CommentEventProcessor.class);

    protected CommentData commentData;
    protected ResourceType commentedResourceType;
    protected long credentialId;
    protected long competenceId;
    protected long activityId;

    protected Context context;

    public CommentEventProcessor(Event event, NotificationManager notificationManager,
                                 NotificationsSettingsManager notificationsSettingsManager,
                                 UrlIdEncoder idEncoder, CommentManager commentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        context = ContextJsonParserService.parseContext(event.getContext());
        credentialId = Context.getIdFromSubContextWithName(context, ContextName.CREDENTIAL);
        competenceId = Context.getIdFromSubContextWithName(context, ContextName.COMPETENCE);
        activityId = Context.getIdFromSubContextWithName(context, ContextName.ACTIVITY);

        Optional<CommentData> optionalComment = commentManager.getCommentData(event.getObject().getId(), event.getActorId());

        if (optionalComment.isPresent()) {
            this.commentData = optionalComment.get();
            setCommentedResourceType();
        }
    }

    private void setCommentedResourceType() {
        switch (commentData.getCommentedResourceType()) {
            case Activity:
                commentedResourceType = ResourceType.Activity;
                break;
            case Competence:
                commentedResourceType = ResourceType.Competence;
                break;
            case SocialActivity:
                commentedResourceType = ResourceType.SocialActivity;
                break;
            case ActivityResult:
                commentedResourceType = ResourceType.ActivityResult;
                break;
        }
    }

    @Override
    NotificationSenderData getSenderData() {
        return new NotificationSenderData(event.getActorId(), NotificationActorRole.OTHER);
    }

    @Override
    boolean isAnonymizedActor() {
        return false;
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        if (receiver != 0 && sender != receiver) {
            return true;
        }
        return false;
    }

    @Override
    abstract List<NotificationReceiverData> getReceiversData();

    @Override
    abstract NotificationType getNotificationType();

    @Override
    abstract ResourceType getObjectType();

    @Override
    abstract long getObjectId();

    protected final String getNotificationLink(PageSection section) {
        switch (commentedResourceType) {
            case Activity:
                if (competenceId > 0) {
                    return section.getPrefix() +
                            "/credentials/" + idEncoder.encodeId(credentialId) +
                            "/competences/" + idEncoder.encodeId(competenceId) +
                            "/activities/" + idEncoder.encodeId(commentData.getCommentedResourceId()) +
                            "?comment=" + idEncoder.encodeId(commentData.getCommentId());
                } else {
                    logger.error("Activity comment notification link can't be constructed because competence id is not available in learning context.");
                }
                break;
            case Competence:
                return section.getPrefix() +
                        "/credentials/" + idEncoder.encodeId(credentialId) +
                        "/competences/" + idEncoder.encodeId(commentData.getCommentedResourceId()) +
                        "?comment=" + idEncoder.encodeId(commentData.getCommentId());
            case SocialActivity:
                return section.getPrefix() +
                        "/posts/" + idEncoder.encodeId(commentData.getCommentedResourceId()) +
                        "?comment=" + idEncoder.encodeId(commentData.getCommentId());
            case ActivityResult:
                if (activityId > 0) {
                    if (section.equals(PageSection.STUDENT)) {
                        return section.getPrefix() +
                                "/credentials/" + idEncoder.encodeId(credentialId) +
                                "/competences/" + idEncoder.encodeId(competenceId) +
                                "/activities/" + idEncoder.encodeId(activityId) +
                                "/responses/" + idEncoder.encodeId(commentData.getCommentedResourceId()) +
                                "?comment=" + idEncoder.encodeId(commentData.getCommentId());
                    } else {
                        //for manage section we have a different page than in user section
						/*
						if credential id can't be extracted we don't know to which assessment should we
						send the manager so for now notification is not created in this case
						TODO maybe it doesn't make sense to send manager to assessment page - he can comment
						the response from assessment for one credential and notification can send him to
						the assessment for other credential.
						 */
                        if (credentialId > 0) {
                            return section.getPrefix() +
                                    "/credentials/" + idEncoder.encodeId(credentialId) +
                                    "/assessments/activities/" + idEncoder.encodeId(activityId) +
                                    "/" + idEncoder.encodeId(commentData.getCommentedResourceId())
                                    + "?comment=" + idEncoder.encodeId(commentData.getCommentId());
                        }
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

}
