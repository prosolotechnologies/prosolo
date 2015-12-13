package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

public class PostEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(PostEventProcessor.class);

	public PostEventProcessor(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	protected void setResource() {
		this.resource = (Post) event.getObject();
	}

	@Override
	protected void setNotificationType() {
		this.notificationType = EventType.MENTIONED;
	}
	
	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		Post post = (Post) resource;
		if (post.getMentionedUsers() != null) {
			for (User user : post.getMentionedUsers()) {
				users.add(user);
			}
		}
		return users;
	}

	@Override
	User getSender() {
		return event.getActor();
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
