package org.prosolo.services.messaging.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.messaging.data.SessionMessage;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.services.messaging.MessageHandler;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationCacheUpdater;
import org.prosolo.services.user.ActiveUsersSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.Set;

@Service("org.prosolo.services.messaging.SessionMessageHandler")
public class SessionMessageHandlerImpl implements MessageHandler<SessionMessage> {

	private static Logger logger = Logger.getLogger(SessionMessageHandlerImpl.class.getName());

	@Inject private ActiveUsersSessionRegistry activeUsersSessionRegistry;
	@Autowired
	private DefaultManager defaultManager;
	@Autowired
	private MessageInboxUpdater messageInboxUpdater;
	@Autowired
	private NotificationCacheUpdater notificationCacheUpdater;

	@Override
	public void handle(SessionMessage message) throws WorkerException {
		Session session = (Session) defaultManager.getPersistence().openSession();

		long receiverId = message.getReceiverId();
		Set<HttpSession> userSessions = activeUsersSessionRegistry.getAllUserSessions(receiverId);
		try {
			long resourceId = message.getResourceId();

			switch (message.getServiceType()) {
			case DIRECT_MESSAGE:
				if (!userSessions.isEmpty()) {
					Message directMessage = (Message) session.load(Message.class, resourceId);

					MessageThread messagesThread = directMessage.getMessageThread();
					messagesThread = (MessageThread) session.merge(messagesThread);
					for (HttpSession httpSession : userSessions) {
						messageInboxUpdater.updateOnNewMessage(directMessage, messagesThread, httpSession);
					}
				}
				break;
			case ADD_NEW_MESSAGE_THREAD:
				if (!userSessions.isEmpty()) {
					MessageThread messagesThread = (MessageThread) session.load(MessageThread.class, resourceId);
					for (HttpSession httpSession : userSessions) {
						messageInboxUpdater.addNewMessageThread(messagesThread, httpSession);
					}
				}
				break;
			case ADD_NOTIFICATION:
				for (HttpSession httpSession : userSessions) {
					notificationCacheUpdater.updateNotificationData(resourceId, httpSession, session);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
			throw new WorkerException();
		} finally {
			HibernateUtil.close(session);
		}
	}

}
