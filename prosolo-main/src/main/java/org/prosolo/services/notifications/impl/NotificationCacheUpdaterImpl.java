package org.prosolo.services.notifications.impl;

import java.io.Serializable;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationCacheUpdater;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service("org.prosolo.services.notifications.NotificationCacheUpdater")
public class NotificationCacheUpdaterImpl implements NotificationCacheUpdater, Serializable {
	
	private Logger logger = Logger.getLogger(NotificationCacheUpdaterImpl.class);
	
	private static final long serialVersionUID = -2234740595717219169L;
	
	@Inject
	private DefaultManager defaultManager;

	@Override
	public void updateNotificationData(long notificationId, HttpSession userSession, Session session) throws ResourceCouldNotBeLoadedException {
		if (userSession != null) {
			TopNotificationsBean topNotificationsBean = (TopNotificationsBean) userSession.getAttribute("topNotificationsBean");
			LoggedUserBean loggedUserBean = (LoggedUserBean) userSession.getAttribute("loggeduser");

			if (topNotificationsBean != null) {
				try {
					Notification notification = (Notification) defaultManager.loadResource(Notification.class, notificationId, true);
					
					NotificationData notificationData = NotificationDataConverter.convertNotification(
							loggedUserBean.getUser(), 
							notification, 
							session, 
							loggedUserBean.getLocale());
					
					topNotificationsBean.addNotification(notificationData, session);
				} catch (NotificationNotSupported e) {
					logger.error(e);
				}
			}
		}
	}
	
}