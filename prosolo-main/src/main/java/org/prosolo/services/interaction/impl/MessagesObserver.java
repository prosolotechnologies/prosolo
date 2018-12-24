package org.prosolo.services.interaction.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.user.ActiveUsersSessionRegistry;
import org.prosolo.web.messaging.data.MessageThreadParticipantData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

/**
 * Event observer responding to events related to messaging functionality.
 *
 * @author Zoran Jeremic
 * @date 2013-05-18
 * @since 0.5
 */
@Service("org.prosolo.services.interaction.MessagesObserver")
public class MessagesObserver extends EventObserver {
    private static Logger logger = Logger.getLogger(MessagesObserver.class);

    @Inject
    private MessagingManager messagingManager;
    @Inject
    private SessionMessageDistributer messageDistributer;
    @Inject
    private MessageInboxUpdater messageInboxUpdater;
    @Inject
    private ActiveUsersSessionRegistry activeUsersSessionRegistry;

    @Override
    public EventType[] getSupportedEvents() {
        return new EventType[]{
                EventType.SEND_MESSAGE,
        };
    }

    @Override
    public Class<? extends BaseEntity>[] getResourceClasses() {
        return null;
    }

    @Override
    public void handleEvent(Event event) {
        Session session = (Session) messagingManager.getPersistence().openSession();

        try {
            if (event.getAction().equals(EventType.SEND_MESSAGE)) {
                long messageId = event.getObject().getId();
                long senderId = messagingManager.getSenderId(messageId, session);
                List<MessageThreadParticipantData> participants = messagingManager.getThreadParticipantsForMessage(messageId, session);

                for (MessageThreadParticipantData participant : participants) {

                    //don't send the message to the message sender
                    if (senderId != participant.getId()) {
                        if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(ServiceType.DIRECT_MESSAGE, participant.getId(), 0, null, null);
                        } else {
							Set<HttpSession> userSessions = activeUsersSessionRegistry.getAllUserSessions(participant.getId());
							for (HttpSession httpSession : userSessions) {
								messageInboxUpdater.updateOnNewMessage(httpSession);
							}
                        }
                    }
                }
            }
            session.flush();
        } catch (Exception e) {
            logger.error("Exception in handling message", e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}

