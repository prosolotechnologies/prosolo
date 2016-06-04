package org.prosolo.web.notification;

import java.util.Iterator;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.primefaces.context.RequestContext;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "topNotificationsBean1")
@Component("topNotificationsBean1")
@Scope("session")
public class TopNotificationsBean1 {

	private static Logger logger = Logger.getLogger(TopNotificationsBean1.class);

	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private NotificationManager notificationsManager;

	private LinkedList<NotificationData> notificationDatas;
	private int unreadNotificationsNo;

	private int notificationsLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private int refreshRate = Settings.getInstance().config.application.notificationsRefreshRate;

	@PostConstruct
	public void init() {
		initNotificationsNo();
	}

	private void initNotificationsNo() {
		logger.debug("Initializing unread notifications number.");

		User user = loggedUser.getUser();

		if (user != null)
			this.unreadNotificationsNo = notificationsManager.getNumberOfUnreadNotifications(user);
	}

	public synchronized void addNotification(NotificationData notificationData, Session session) {
		if (notificationDatas == null) {
			fetchNotifications();
		} else {
			notificationDatas.addFirst(notificationData);
		}

		unreadNotificationsNo++;

		if (notificationDatas.size() > notificationsLimit) {
			Iterator<NotificationData> iterator = notificationDatas.iterator();
			int index = 1;

			while (iterator.hasNext()) {
				iterator.next();

				if (index > notificationsLimit) {
					iterator.remove();
				}
				index++;
			}
		}
	}

	public void fetchNotifications() {
		if (notificationDatas == null) {
			logger.debug("Initializing notifications.");

			try {
				this.notificationDatas = (LinkedList<NotificationData>) notificationsManager.getNotificationsForUser(
						loggedUser.getUser().getId(), 0, notificationsLimit, null, loggedUser.getLocale());
			} catch (DbConnectionException e) {
				logger.error(e);
			}
		}
		unreadNotificationsNo = 0;
	}

	public LinkedList<NotificationData> getNotificationDatas() {
		return notificationDatas;
	}

	public void setNotificationDatas(LinkedList<NotificationData> notificationDatas) {
		this.notificationDatas = notificationDatas;
	}

	public int getUnreadNotificationsNo() {
		return unreadNotificationsNo;
	}

	public void setUnreadNotificationsNo(int unreadNotificationsNo) {
		this.unreadNotificationsNo = unreadNotificationsNo;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}
	
}
