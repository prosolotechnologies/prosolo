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
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.data.profile.ProfileSettingsData;
import org.prosolo.web.util.page.PageSection;

import java.util.List;
import java.util.Optional;

public class FollowUserEventProcessor extends SimpleNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(FollowUserEventProcessor.class);

    private StudentProfileManager studentProfileManager;

    public FollowUserEventProcessor(Event event, NotificationManager notificationManager,
                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                    StudentProfileManager studentProfileManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);
        this.studentProfileManager = studentProfileManager;
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
        return new NotificationSenderData(getSenderId(), NotificationActorRole.OTHER);
    }

    @Override
    boolean isAnonymizedActor() {
        return false;
    }

    private long getSenderId() {
        return event.getActorId();
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.Follow_User;
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
        Optional<ProfileSettingsData> profileSettings = studentProfileManager.getProfileSettingsData(getSenderId());
        return "/p/" + profileSettings.get().getCustomProfileUrl();
    }

}
