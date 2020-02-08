package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public class CommentLikeEventProcessor extends CommentEventProcessor {

    private static Logger logger = Logger.getLogger(CommentLikeEventProcessor.class);

    public CommentLikeEventProcessor(Event event, NotificationManager notificationManager,
                                     NotificationsSettingsManager notificationsSettingsManager, Activity1Manager activityManager,
                                     UrlIdEncoder idEncoder, CommentManager commentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder, commentManager);
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        try {
            PageSection section = commentData.isManagerComment() ? PageSection.MANAGE : PageSection.STUDENT;
            String notificationLink = getNotificationLink(section);

            if (notificationLink != null && !notificationLink.isEmpty()) {
                Long resCreatorId = commentData.getCreator().getId();
                return List.of(new NotificationReceiverData(resCreatorId, notificationLink, false, section));
            }
            return List.of();
        } catch (Exception e) {
            logger.error("Error", e);
            return new ArrayList<>();
        }
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.Comment_Like;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Comment;
    }

    @Override
    long getObjectId() {
        return commentData.getCommentId();
    }

}
