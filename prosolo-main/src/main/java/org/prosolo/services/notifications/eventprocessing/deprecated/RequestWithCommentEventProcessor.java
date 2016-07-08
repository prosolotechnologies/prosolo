package org.prosolo.services.notifications.eventprocessing.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

@Deprecated
public class RequestWithCommentEventProcessor extends NotificationEventProcessor {

	public RequestWithCommentEventProcessor(Event event, Session session, 
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	protected void setResource() {
		this.resource = (Request) session.merge(event.getObject());
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		Request request = (Request) resource;
		users.add(request.getSentTo());
		return users;
	}

	@Override
	User getSender() {
//		return event.getActor();
		return null;
	}

	@Override
	String getNotificationMessage() {
		return ((Request) resource).getComment();
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		return true;
	}
	

}
