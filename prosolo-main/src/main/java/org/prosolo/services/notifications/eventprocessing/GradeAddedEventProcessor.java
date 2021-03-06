package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public abstract class GradeAddedEventProcessor extends AssessmentNotificationEventProcessor {

    private static Logger logger = Logger.getLogger(GradeAddedEventProcessor.class);

    public GradeAddedEventProcessor(Event event, NotificationManager notificationManager,
                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return sender != receiver;
    }

    protected abstract boolean shouldNotificationBeGenerated();
    protected abstract long getStudentId();
    protected abstract long getAssessorId();
    protected abstract String getNotificationLink();


    @Override
    List<NotificationReceiverData> getReceiversData() {
        try {
            // this notification should be created only if it is not self-assessment
            if (shouldNotificationBeGenerated()) {
                return List.of(new NotificationReceiverData(getStudentId(), getNotificationLink(), false, PageSection.STUDENT));
            }
            return List.of();
        } catch (Exception e) {
            logger.error("Error", e);
            return new ArrayList<>();
        }
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.GradeAdded;
    }

}
