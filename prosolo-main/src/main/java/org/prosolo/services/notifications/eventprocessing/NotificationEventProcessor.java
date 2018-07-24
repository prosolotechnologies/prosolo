package org.prosolo.services.notifications.eventprocessing;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

public abstract class NotificationEventProcessor {

    protected Event event;
    protected Session session;
    protected NotificationManager notificationManager;
    private NotificationsSettingsManager notificationsSettingsManager;
    protected UrlIdEncoder idEncoder;

    public NotificationEventProcessor(Event event, Session session,
                                      NotificationManager notificationManager,
                                      NotificationsSettingsManager notificationsSettingsManager,
                                      UrlIdEncoder idEncoder) {
        this.event = event;
        this.session = session;
        this.notificationManager = notificationManager;
        this.notificationsSettingsManager = notificationsSettingsManager;
        this.idEncoder = idEncoder;
    }

    public List<Notification1> getNotificationList() {
        List<Notification1> notifications = new ArrayList<>();
        List<NotificationReceiverData> receivers = getReceiversData();
        if (!receivers.isEmpty()) {
            long sender = getSenderId();
            NotificationType notificationType = getNotificationType();
            long objectId = getObjectId();
            ResourceType resType = getObjectType();
            long targetId = getTargetId();
            ResourceType targetType = getTargetType();

            for (NotificationReceiverData receiver : receivers) {
                if (isConditionMet(sender, receiver.getReceiverId()) && receiver.getNotificationLink() != null) {
                    Notification1 notification = notificationManager.createNotification(
                            sender,
                            receiver.getReceiverId(),
                            notificationType,
                            event.getDateCreated(),
                            objectId,
                            resType,
                            targetId,
                            targetType,
                            receiver.getNotificationLink(),
                            notificationsSettingsManager
                                    .shouldUserReceiveEmail(receiver.getReceiverId(), notificationType, session),
                            receiver.isObjectOwner(),
                            session,
                            receiver.getPageSection());

                    notifications.add(notification);
                }
            }
        }

        return notifications;
    }

    abstract boolean isConditionMet(long sender, long receiver);

    abstract List<NotificationReceiverData> getReceiversData();

    abstract long getSenderId();

    abstract NotificationType getNotificationType();

    abstract ResourceType getObjectType();

    abstract long getObjectId();

    public ResourceType getTargetType() {
        return null;
    }

    public long getTargetId() {
        return 0;
    }

}
