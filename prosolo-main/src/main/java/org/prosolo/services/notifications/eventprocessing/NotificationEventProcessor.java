package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.prosolo.services.notifications.eventprocessing.data.NotificationDeliveryTypes;

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
				NotificationDeliveryTypes ndt = getNotificationDeliveryTypesForUser(receiver);
				if(ndt.isUi() || ndt.isEmail()) {
					Notification notification = notificationManager.createNotification(
							resource,
							sender, 
							receiver,
							notificationType, 
							getNotificationMessage(),
							event.getDateCreated(), 
							ndt.isUi(),
							ndt.isEmail(),
							session);
					
					afterProcessing(notification, session);
					notifications.add(notification);
				}
			}
		}
		
		return notifications;
	}
	
	private NotificationDeliveryTypes getNotificationDeliveryTypesForUser(User receiver) {
		
		UserNotificationsSettings userNotificationSettings = notificationsSettingsManager
				.getOrCreateNotificationsSettings(receiver, session);

		if(userNotificationSettings != null) {
			List<NotificationSettings> settings = userNotificationSettings
					.getNotificationsSettings();
			return getNotificationDeliveryTypesFromNotificationSettingsList(settings);
		}
		return new NotificationDeliveryTypes();
	}

	private NotificationDeliveryTypes getNotificationDeliveryTypesFromNotificationSettingsList(List<NotificationSettings> settings) {
		NotificationDeliveryTypes ndt = new NotificationDeliveryTypes();
		
		if(settings != null) {
			for(NotificationSettings ns : settings) {
				if(event.getAction() == ns.getType()) {
					ndt.setUi(ns.isSubscribedUI());
					ndt.setEmail(ns.isSubscribedEmail());
					return ndt;
				}
			}
		}
		return ndt;
	}

	abstract boolean isConditionMet(User sender, User receiver);

	abstract List<User> getReceivers();
	
	abstract User getSender();
	
	abstract String getNotificationMessage();
	
	abstract void afterProcessing(Notification notification, Session session);
}
