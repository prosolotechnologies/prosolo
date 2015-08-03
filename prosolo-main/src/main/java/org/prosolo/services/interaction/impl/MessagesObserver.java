package org.prosolo.services.interaction.impl;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.MessagesBean;
import org.prosolo.web.notification.TopInboxBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author Zoran Jeremic 2013-05-18
 */
@Service("org.prosolo.services.interaction.MessagesObserver")
public class MessagesObserver implements EventObserver {
	private static Logger logger = Logger.getLogger(MessagesObserver.class);
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SessionMessageDistributer messageDistributer;
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
				SimpleOfflineMessage message = (SimpleOfflineMessage) event.getObject();
				MessagesThread messagesThread = message.getMessageThread();
				messagesThread = (MessagesThread) session.merge(messagesThread);
				
				if (!messagesThread.getMessages().contains(message)) {
					messagesThread.addMessage(message);
					session.save(messagesThread);
				}
				
				User receiver = message.getReceiver();
				
				if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
					messageDistributer.distributeMessage(ServiceType.DIRECTMESSAGE, receiver.getId(), message.getId(), null, null);
				} else {
					HttpSession httpSession = applicationBean.getUserSession(receiver.getId());
					
					if (httpSession != null) {
						TopInboxBean inboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
						
						if (inboxBean != null) {
							inboxBean.updateMessageThread(messagesThread);
						}
						
						MessagesBean messagesBean = (MessagesBean) httpSession.getAttribute("messagesBean");
						
						if (messagesBean != null) {
							
							if (messagesBean.getThreadData().getId() == messagesThread.getId()) {
								messagesBean.addMessage(message);
							}
						}
					}
					
				}
			} else if (event.getAction().equals(EventType.START_MESSAGE_THREAD)) {
				MessagesThread messagesThread = (MessagesThread) event.getObject();

				if (messagesThread != null) {
					List<User> participants = messagesThread.getParticipants();
					
					for (User participant : participants) {
						HttpSession httpSession = applicationBean.getUserSession(participant.getId());
						
						if (httpSession != null) {
							TopInboxBean topInboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
							
							if (topInboxBean != null) {
								topInboxBean.addNewMessageThread(messagesThread);
							}
						} else if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(
									ServiceType.ADDNEWMESSAGETHREAD, 
									participant.getId(), 
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

