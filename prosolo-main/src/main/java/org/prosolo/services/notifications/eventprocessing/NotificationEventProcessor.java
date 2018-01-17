package org.prosolo.services.notifications.eventprocessing;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.util.page.PageSection;

import javax.inject.Inject;
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
                    //				String section = getUrlSection(receiver);
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
                            shouldUserBeNotifiedByEmail(receiver.getReceiverId(), notificationType),
                            receiver.isObjectOwner(),
                            session,
                            receiver.getPageSection());

                    notifications.add(notification);
                }
            }
        }

        return notifications;
    }

    private boolean shouldUserBeNotifiedByEmail(long receiverId, NotificationType type) {
        Transaction transaction = null;
        NotificationSettings emailNotificationSettings = null;
        try {
            transaction = session.beginTransaction();
            emailNotificationSettings = notificationsSettingsManager
                    .getOrCreateNotificationSettings(receiverId, type, session);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }

        if (emailNotificationSettings != null) {
            return emailNotificationSettings.isSubscribedEmail();
        }
        return false;
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
