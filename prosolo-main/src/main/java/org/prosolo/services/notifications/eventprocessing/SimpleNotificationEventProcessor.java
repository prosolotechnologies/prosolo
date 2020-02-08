package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleNotificationEventProcessor implements NotificationEventProcessor {

    protected Event event;
    protected NotificationManager notificationManager;
    private NotificationsSettingsManager notificationsSettingsManager;
    protected UrlIdEncoder idEncoder;

    public SimpleNotificationEventProcessor(Event event, NotificationManager notificationManager,
                                            NotificationsSettingsManager notificationsSettingsManager,
                                            UrlIdEncoder idEncoder) {
        this.event = event;
        this.notificationManager = notificationManager;
        this.notificationsSettingsManager = notificationsSettingsManager;
        this.idEncoder = idEncoder;
    }

    @Override
    public List<Notification1> getNotificationList() {
        List<Notification1> notifications = new ArrayList<>();
        List<NotificationReceiverData> receivers = getReceiversData();

        if (!receivers.isEmpty()) {
            NotificationSenderData sender = getSenderData();
            NotificationType notificationType = getNotificationType();
            long objectId = getObjectId();
            ResourceType resType = getObjectType();
            long targetId = getTargetId();
            ResourceType targetType = getTargetType();

            for (NotificationReceiverData receiver : receivers) {
                if (isConditionMet(sender.getSenderId(), receiver.getReceiverId()) && receiver.getNotificationLink() != null) {
                    Notification1 notification = notificationManager.createNotification(
                            sender.getSenderId(),
                            sender.getActorRole(),
                            isAnonymizedActor(),
                            receiver.getReceiverId(),
                            notificationType,
                            event.getDateCreated(),
                            objectId,
                            resType,
                            targetId,
                            targetType,
                            receiver.getNotificationLink(),
                            notificationsSettingsManager
                                    .shouldUserReceiveEmail(receiver.getReceiverId(), notificationType),
                            receiver.isObjectOwner(),
                            receiver.getPageSection());

                    notifications.add(notification);
                }
            }
        }

        return notifications;
    }

    abstract boolean isConditionMet(long sender, long receiver);

    abstract List<NotificationReceiverData> getReceiversData();

    abstract NotificationSenderData getSenderData();

    abstract NotificationType getNotificationType();

    abstract ResourceType getObjectType();

    abstract long getObjectId();

    abstract boolean isAnonymizedActor();

    public ResourceType getTargetType() {
        return null;
    }

    public long getTargetId() {
        return 0;
    }

}
