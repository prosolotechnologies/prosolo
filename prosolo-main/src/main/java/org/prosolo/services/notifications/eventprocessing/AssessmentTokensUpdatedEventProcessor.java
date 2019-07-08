package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-07-08
 * @since 1.3.2
 */
public class AssessmentTokensUpdatedEventProcessor extends SimpleNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AssessmentTokensUpdatedEventProcessor.class);

    public AssessmentTokensUpdatedEventProcessor(Event event, NotificationManager notificationManager,
                                                 NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        return List.of(new NotificationReceiverData(event.getObject().getId(), getNotificationLink(), false, PageSection.STUDENT));
    }

    @Override
    NotificationSenderData getSenderData() {
        return new NotificationSenderData(0, NotificationActorRole.OTHER);
    }

    @Override
    boolean isAnonymizedActor() {
        return false;
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.ASSESSMENT_TOKENS_NUMBER_UPDATED;
    }

    @Override
    ResourceType getObjectType() {
        return null;
    }

    @Override
    long getObjectId() {
        return 0;
    }


    private String getNotificationLink() {
        return "/assessments/my/credentials";
    }

}
