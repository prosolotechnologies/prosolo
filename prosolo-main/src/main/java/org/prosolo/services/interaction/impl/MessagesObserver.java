package org.prosolo.services.interaction.impl;

import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author Zoran Jeremic 2013-05-18
 */
@Service("org.prosolo.services.interaction.MessagesObserver")
public class MessagesObserver extends EventObserver {
	private static Logger logger = Logger.getLogger(MessagesObserver.class);
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private MessageInboxUpdater messageInboxUpdater;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
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
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		try {
			if (event.getAction().equals(EventType.SEND_MESSAGE)) {
				Message message = (Message) event.getObject();
				MessageThread messagesThread = message.getMessageThread();
				messagesThread = (MessageThread) session.merge(messagesThread);
				
				Set<ThreadParticipant> participants = messagesThread.getParticipants();
				
				for (ThreadParticipant participant : participants) {
					User user = participant.getUser();
					if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
						//don't send message to the sender of the original message
						if(user.getId() != message.getSender().getId()){
							messageDistributer.distributeMessage(ServiceType.DIRECT_MESSAGE, user.getId(), message.getId(), null, null);
						}
					} else {
						HttpSession httpSession = applicationBean.getUserSession(user.getId());
						
						messageInboxUpdater.updateOnNewMessage(message, messagesThread, httpSession);
					}
				}
			} else if (event.getAction().equals(EventType.START_MESSAGE_THREAD)) {
				MessageThread messagesThread = (MessageThread) event.getObject();

				if (messagesThread != null) {
					Set<ThreadParticipant> participants = messagesThread.getParticipants();
					
					for (ThreadParticipant participant : participants) {
						User user = participant.getUser();
						HttpSession httpSession = applicationBean.getUserSession(user.getId());
						
						if (httpSession != null) {
							messageInboxUpdater.addNewMessageThread(messagesThread, httpSession);
						} else if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(
									ServiceType.ADD_NEW_MESSAGE_THREAD, 
									user.getId(), 
									messagesThread.getId(), 
									null,
									null);
						}
					}
				}
			}
			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		}
		
		finally {
			HibernateUtil.close(session);
		}
	}
}

