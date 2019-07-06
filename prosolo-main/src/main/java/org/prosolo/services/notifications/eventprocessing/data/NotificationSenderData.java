package org.prosolo.services.notifications.eventprocessing.data;

import org.prosolo.common.domainmodel.user.notifications.NotificationActorRole;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-06-18
 * @since 1.2.0
 */
public class NotificationSenderData implements Serializable {

    private final long senderId;
    private final NotificationActorRole actorRole;

    public NotificationSenderData(long senderId, NotificationActorRole actorRole) {
        this.senderId = senderId;
        this.actorRole = actorRole;
    }

    public long getSenderId() {
        return senderId;
    }

    public NotificationActorRole getActorRole() {
        return actorRole;
    }

}
