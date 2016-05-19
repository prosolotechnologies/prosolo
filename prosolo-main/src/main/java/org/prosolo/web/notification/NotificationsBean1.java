package org.prosolo.web.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Deprecated
@ManagedBean(name = "notificationsBean")
@Component("notificationsBean")
@Scope("view")
public class NotificationsBean1 implements Serializable {
	
	private static final long serialVersionUID = 1560861635204558368L;
	private static Logger logger = Logger.getLogger(NotificationsBean1.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private NotificationManager notificationsManager;
	@Autowired private DefaultManager defaultManager;
	private boolean moreToLoad;
	
	private List<NotificationData> notifications = new ArrayList<NotificationData>();
	private final int limit = 10;
	private int page = 0;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing");
		loadNotifications();
	}
	
	public void loadNotifications() {
		List<NotificationData> notificationData = null;
		
		try {
			notificationData = NotificationDataConverter.convertNotifications(
					loggedUser.getUser(),
					notificationsManager.getNotifications(loggedUser.getUser(), page , limit+1),
					(Session) defaultManager.getPersistence().currentManager(),
					loggedUser.getLocale());
		} catch (NotificationNotSupported e) {
			logger.error(e);
		}
		 
		if (notificationData != null && !notificationData.isEmpty()) {
			// if there is more than limit, set moreToLoad to true
			if (notificationData.size() == limit + 1) {
				notificationData = notificationData.subList(0, notificationData.size() - 1);
				moreToLoad = true;
			} else {
				moreToLoad = false;
			}
			notifications.addAll(notificationData);
		}
	}

	public void loadMoreNotifications() {
		page++;
		loadNotifications();
	}

	/*
	 * GETTERS/SETTERS
	 */
	public List<NotificationData> getNotifications() {
		return notifications;
	}
	
	public void setNotifications(List<NotificationData> notifications) {
		this.notifications = notifications;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}
	
}
