package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class NotificationEventProcessor {

	protected Event event;
	protected Session session;
	
	protected NotificationManager notificationManager;
	private NotificationsSettingsManager notificationsSettingsManager;
	protected UrlIdEncoder idEncoder;
	
	public NotificationEventProcessor(Event event, Session session, 
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager,
			UrlIdEncoder idEncoder) {
		this.event = event;
		this.session = session;
		this.notificationManager = notificationManager;
		this.notificationsSettingsManager = notificationsSettingsManager;
		this.idEncoder = idEncoder;
	}

	public List<Notification1> getNotificationList() {
		List<Notification1> notifications = new ArrayList<>();
		List<Long> receivers = getReceiverIds();
		
		for(long receiver : receivers) {
			long sender = getSenderId();
			if(isConditionMet(sender, receiver)) {
				String section = getUrlSection(receiver);
				Notification1 notification = notificationManager.createNotification(
						sender, 
						receiver,
						getNotificationType(), 
						event.getDateCreated(),
						getObjectId(),
						getObjectType(),
						section + getNotificationLink(),
						shouldUserBeNotifiedByEmail(receiver),
						session);
				
				notifications.add(notification);
			}
		}
		
		return notifications;
	}
	
	private boolean shouldUserBeNotifiedByEmail(long receiverId) {
		
		UserNotificationsSettings userNotificationSettings = notificationsSettingsManager
				.getOrCreateNotificationsSettings(receiverId, session);

		if(userNotificationSettings != null) {
			List<NotificationSettings> settings = userNotificationSettings
					.getNotifications();
			return determineIfUserShouldBeNotifiedByEmail(settings);
		}
		return false;
	}

	private boolean determineIfUserShouldBeNotifiedByEmail(List<NotificationSettings> settings) {
		if(settings != null) {
			for(NotificationSettings ns : settings) {
				if(getNotificationType() == ns.getType()) {
					return ns.isSubscribedEmail();
				}
			}
		}
		return false;
	}
	
	/**
	 * When it is important to include section when forming url based on user role, method should be 
	 * overriden in concrete event processor. 
	 * @param userId
	 * @return
	 */
	protected String getUrlSection(long userId) {
		return "";
	}

	abstract boolean isConditionMet(long sender, long receiver);

	abstract List<Long> getReceiverIds();
	
	abstract long getSenderId();
	
	abstract NotificationType getNotificationType();
	
	abstract ObjectType getObjectType();
	
	abstract long getObjectId();
	
	abstract String getNotificationLink();
	
}
