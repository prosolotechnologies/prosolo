package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

public abstract class NotificationEventProcessor {

	protected Event event;
	protected EventType notificationType;
	protected BaseEntity resource;
	protected Session session;
	
	protected NotificationManager notificationManager;
	private NotificationsSettingsManager notificationsSettingsManager;
	
	public NotificationEventProcessor(Event event, Session session, 
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager) {
		this.event = event;
		this.session = session;
		this.notificationManager = notificationManager;
		this.notificationsSettingsManager = notificationsSettingsManager;
		setResource();
		setNotificationType();
		
	}
	
	protected void setNotificationType() {
		this.notificationType = event.getAction();
	}

	protected void setResource() {
		this.resource = event.getObject();
	}

	public List<Notification> getNotificationList() {
		List<Notification> notifications = new ArrayList<>();
		List<User> receivers = getReceivers();
		
		for(User receiver : receivers) {
			User sender = getSender();
			if(isConditionMet(sender, receiver)) {
				Notification notification = notificationManager.createNotification(
						resource,
						sender, 
						receiver,
						notificationType, 
						getNotificationMessage(),
						event.getDateCreated(), 
						session);
				
				afterProcessing(notification, session);
				notifications.add(notification);
			}
		}
		
		return notifications;
	}


	protected boolean isConditionMet(User sender, User receiver) {
		return isAdditionalConditionMet(sender, receiver) && isReceiverRegisteredForNotification(receiver);
	};
	
	private boolean isReceiverRegisteredForNotification(User receiver) {
		UserNotificationsSettings userNotificationSettings = notificationsSettingsManager
				.getOrCreateNotificationsSettings(receiver);
		if(userNotificationSettings != null) {
			List<NotificationSettings> settings = userNotificationSettings
					.getNotificationsSettings();
			return isUserRegisteredForNotification(settings);
		}
		return false;
	}

	private boolean isUserRegisteredForNotification(List<NotificationSettings> settings) {
		if(settings != null) {
			for(NotificationSettings ns : settings) {
				if(event.getAction() == ns.getType()) {
					return true;
				}
			}
		}
		return false;
	}

	abstract boolean isAdditionalConditionMet(User sender, User receiver);

	abstract List<User> getReceivers();
	
	abstract User getSender();
	
	abstract String getNotificationMessage();
	
	abstract void afterProcessing(Notification notification, Session session);
}
