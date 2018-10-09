package org.prosolo.services.interaction.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.messaging.data.MessageThreadParticipantData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

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

    @Autowired
    private ApplicationBean applicationBean;
    @Autowired
    private MessagingManager messagingManager;
    @Autowired
    private SessionMessageDistributer messageDistributer;
    @Autowired
    private MessageInboxUpdater messageInboxUpdater;

    @Override
    public EventType[] getSupportedEvents() {
        return new EventType[]{
                EventType.SEND_MESSAGE,
                EventType.START_MESSAGE_THREAD,
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
                            HttpSession httpSession = applicationBean.getUserSession(participant.getId());

                            messageInboxUpdater.updateOnNewMessage(httpSession);
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

