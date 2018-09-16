package org.prosolo.web.notification;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.LoggedUserBean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class TopNotificationsBean1 {

	private static Logger logger = Logger.getLogger(TopNotificationsBean1.class);

	public abstract NotificationSection getSection();

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private NotificationManager notificationsManager;

	private int notificationsLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private String domainPrefix = CommonSettings.getInstance().config.appConfig.domain.substring(0, CommonSettings.getInstance().config.appConfig.domain.length()-1);

	private LinkedList<NotificationData> notificationData;
	private int unreadNotificationsNo;

	@PostConstruct
	public void init(){
		initNotificationsNo();
		fetchNotifications();
	}

	public void initNotificationsNo() {
		logger.debug("Initializing unread notifications number.");

		if (loggedUser.isLoggedIn())
			this.unreadNotificationsNo = notificationsManager.getNumberOfUnreadNotifications(loggedUser.getUserId(), getSection());
	}

	public void fetchNotifications() {
		logger.debug("Initializing notifications.");

		try {
			this.notificationData = (LinkedList<NotificationData>) notificationsManager.getNotificationsForUser(
					loggedUser.getUserId(), 0, notificationsLimit, null, loggedUser.getLocale(), getSection());
		} catch (DbConnectionException e) {
			logger.error(e);
		}
	}

	public synchronized void addNotification(NotificationData notificationData, Session session) {
		if (this.notificationData == null) {
			fetchNotifications();
		} else {
			this.notificationData.addFirst(notificationData);
		}

		unreadNotificationsNo++;

		if (this.notificationData.size() > notificationsLimit) {
			Iterator<NotificationData> iterator = this.notificationData.iterator();
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

	public void markNotificationsAsRead() {
		unreadNotificationsNo = 0;
	}

	public LinkedList<NotificationData> getNotificationData() {
		return notificationData;
	}

	public int getUnreadNotificationsNo() {
		return unreadNotificationsNo;
	}

	public String getDomainPrefix() {
		return domainPrefix;
	}
}
