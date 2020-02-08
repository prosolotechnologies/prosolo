package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.AnnouncementPublishMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnouncementPublishedEventProcessor extends SimpleNotificationEventProcessor {

    private static Logger logger = Logger.getLogger(AnnouncementPublishedEventProcessor.class);

    private CredentialManager credentialManager;
    private AnnouncementManager announcementManager;

    public AnnouncementPublishedEventProcessor(Event event, NotificationManager notificationManager,
                                               NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                               CredentialManager credentialManager, AnnouncementManager announcementManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);
        this.credentialManager = credentialManager;
        this.announcementManager = announcementManager;
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        List<NotificationReceiverData> receivers = new ArrayList<>();

        try {
            Optional<AnnouncementData> announcement = announcementManager.loadAnnouncement(event.getObject().getId());

            if (announcement.isPresent()) {
                AnnouncementPublishMode mode = announcement.get().getPublishMode();
                Long credId = Long.parseLong(event.getParameters().get("credentialId"));

                List<Long> users;

                if (mode.equals(AnnouncementPublishMode.ALL_STUDENTS)) {
                    users = credentialManager.getUserIdsForCredential(credId);
                } else {
                    users = credentialManager.getActiveUserIdsForCredential(credId);
                }

                String link = getNotificationLink();

                for (long id : users) {
                    receivers.add(new NotificationReceiverData(id, link, false, PageSection.STUDENT));
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return receivers;
    }

    @Override
    NotificationSenderData getSenderData() {
        // we don't display actor name for announcement notifications
        return new NotificationSenderData(0, NotificationActorRole.OTHER);
    }

    @Override
    boolean isAnonymizedActor() {
        return false;
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.AnnouncementPublished;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Announcement;
    }

    @Override
    long getObjectId() {
        return event.getObject().getId();
    }

    @Override
    public ResourceType getTargetType() {
        return ResourceType.Credential;
    }

    @Override
    public long getTargetId() {
        return event.getTarget().getId();
    }

    private String getNotificationLink() {
        return "/credentials/" + idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId")))
                + "/announcements/" + idEncoder.encodeId(event.getObject().getId());
    }

}
