package org.prosolo.services.messaging.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.messaging.data.SessionMessage;
import org.prosolo.common.messaging.rabbitmq.WorkerException;
import org.prosolo.core.db.hibernate.HibernateUtil;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.services.messaging.MessageHandler;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationCacheUpdater;
import org.prosolo.services.user.ActiveUsersSessionRegistry;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.Set;

@Service("org.prosolo.services.messaging.SessionMessageHandler")
public class SessionMessageHandlerImpl implements MessageHandler<SessionMessage> {

	private static Logger logger = Logger.getLogger(SessionMessageHandlerImpl.class.getName());

	@Inject
	private ActiveUsersSessionRegistry activeUsersSessionRegistry;
	@Inject
	private DefaultManager defaultManager;
	@Inject
	private MessageInboxUpdater messageInboxUpdater;
	@Inject
	private NotificationCacheUpdater notificationCacheUpdater;

	@Override
	public void handle(SessionMessage message) throws WorkerException {
		long receiverId = message.getReceiverId();
		Set<HttpSession> userSessions = activeUsersSessionRegistry.getAllUserSessions(receiverId);

		if (!userSessions.isEmpty()) {
			Session session = (Session) defaultManager.getPersistence().openSession();

			try {
				long resourceId = message.getResourceId();

				switch (message.getServiceType()) {
					case DIRECT_MESSAGE:
						for (HttpSession httpSession : userSessions) {
							messageInboxUpdater.updateOnNewMessage(httpSession);
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

}
