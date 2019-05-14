package org.prosolo.services.notifications.eventprocessing;

import org.prosolo.common.domainmodel.user.notifications.Notification1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Includes several simple and/or complex notification event processors which it calls for getting the notifications to be generated
 * @author stefanvuckovic
 * @date 2019-05-09
 * @since 1.3
 */
public class ComplexNotificationEventProcessor implements NotificationEventProcessor {

    private List<NotificationEventProcessor> eventProcessors = new ArrayList<>();

    @Override
    public List<Notification1> getNotificationList() {
        return eventProcessors.stream()
                .map(ep -> ep.getNotificationList())
                .flatMap(notifications -> notifications.stream())
                .collect(Collectors.toList());
    }

    public void addNotificationEventProcessor(NotificationEventProcessor eventProcessor) {
        eventProcessors.add(eventProcessor);
    }
}
