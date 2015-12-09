package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

public class ActivityReportAvailableEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(ActivityReportAvailableEventProcessor.class);

	public ActivityReportAvailableEventProcessor(Event event, Session session,
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	protected void setResource() {
		this.resource = null;
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		users.add(event.getActor());
		return users;
	}

	@Override
	User getSender() {
		return null;
	}

	@Override
	String getNotificationMessage() {
		return null;
	}

	@Override
	void afterProcessing(Notification notification, Session session) {
		// no processing needed
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		return true;
	}

}
