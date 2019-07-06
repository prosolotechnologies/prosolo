package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.domainmodel.user.notifications.Notification1;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-05-09
 * @since 1.3
 */
public interface NotificationEventProcessor {

    List<Notification1> getNotificationList();
}
