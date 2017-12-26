package org.prosolo.services.notifications.impl;

import java.io.Serializable;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.notifications.NotificationCacheUpdater;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.notification.ManagerNotificationsBean;
import org.prosolo.web.notification.StudentNotificationsBean;
import org.prosolo.web.notification.TopNotificationsBean1;
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
	private NotificationManager notificationManager;

	@Override
	public void updateNotificationData(long notificationId, HttpSession userSession, Session session) throws ResourceCouldNotBeLoadedException {
		if (userSession != null) {
			StudentNotificationsBean studentNotificationsBean = (StudentNotificationsBean) userSession.getAttribute("studentNotificationsBean");
			ManagerNotificationsBean managerNotificationsBean = (ManagerNotificationsBean) userSession.getAttribute("managerNotificationsBean");
			LoggedUserBean loggedUserBean = (LoggedUserBean) userSession.getAttribute("loggeduser");
			NotificationData notificationData = new NotificationData();

			if(studentNotificationsBean != null || managerNotificationsBean != null) {
				notificationData = notificationManager
						.getNotificationData(notificationId, false, session, loggedUserBean.getLocale());
			}

			switch (notificationData.getSection()) {
				case STUDENT:
					if (studentNotificationsBean != null) {
						try {
							studentNotificationsBean.addNotification(notificationData, session);
						} catch (DbConnectionException e) {
							logger.error(e);
						}
					}
					break;
				case MANAGE:
					if (managerNotificationsBean != null) {
						try {
							managerNotificationsBean.addNotification(notificationData, session);
						} catch (DbConnectionException e) {
							logger.error(e);
						}
					}
					break;
			}
		}
	}
	
}
