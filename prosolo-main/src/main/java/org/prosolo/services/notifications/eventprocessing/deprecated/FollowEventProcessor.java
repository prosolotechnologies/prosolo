package org.prosolo.services.notifications.eventprocessing.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.NotificationManager;

@Deprecated
public class FollowEventProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(FollowEventProcessor.class);
	
	private DefaultManager defaultManager;
	
	public FollowEventProcessor(Event event, Session session, 
					NotificationManager notificationManager, 
					NotificationsSettingsManager notificationsSettingsManager,
					DefaultManager defaultManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
		this.defaultManager = defaultManager;
	}

	@Override
	protected void setResource() {
		this.resource = null;
	}

	@Override
	List<User> getReceivers() {
		try {
			List<User> users = new ArrayList<>();
	
			User follower = defaultManager.loadResource(User.class, event.getObject().getId(), session);
			users.add(follower);
			return users;
		} catch(ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			return new ArrayList<User>();
		}
	}

	@Override
	User getSender() {
		try {
			return defaultManager.loadResource(User.class, event.getActorId(), session);
		} catch(ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			return null;
		}
	}

	@Override
	String getNotificationMessage() {
		return null;
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		return true;
	}
	
}
