package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SocialActivityLikeEventProcessor extends SimpleNotificationEventProcessor {

    private static Logger logger = Logger.getLogger(SocialActivityLikeEventProcessor.class);

    private SocialActivityData1 socialActivity;

    public SocialActivityLikeEventProcessor(Event event, NotificationManager notificationManager,
                                            NotificationsSettingsManager notificationsSettingsManager,
                                            UrlIdEncoder idEncoder, SocialActivityManager socialActivityManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        Optional<SocialActivityData1> socialActivityData = socialActivityManager.getSocialActivityById(event.getObject().getId(), event.getActorId(), Locale.getDefault());

        if (socialActivityData.isPresent()) {
            socialActivity = socialActivityData.get();
        }
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        Long resCreatorId = socialActivity.getActor().getId();
        return List.of(new NotificationReceiverData(resCreatorId, getNotificationLink(), false, PageSection.STUDENT));
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
        } else {
            logger.error("Error determining condition for sending nitification when liking a social activity. Sender: " + sender + ", receiver: " + receiver);
            return false;
        }
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.Social_Activity_Like;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.SocialActivity;
    }

    @Override
    long getObjectId() {
        return socialActivity.getId();
    }

    private String getNotificationLink() {
        return "/posts/" + idEncoder.encodeId(socialActivity.getId());
    }

}
