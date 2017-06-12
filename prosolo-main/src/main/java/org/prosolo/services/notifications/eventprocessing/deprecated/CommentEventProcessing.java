package org.prosolo.services.notifications.eventprocessing.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

@Deprecated
public class CommentEventProcessing extends NotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CommentEventProcessing.class);

	public CommentEventProcessing(Event event, Session session,
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	protected void setResource() {
		if(event.getAction() == EventType.Comment_Reply) {
			this.resource = ((Comment1) session.merge(event.getObject()))
					.getParentComment();
		} else {
			this.resource = (Comment1) session.merge(event.getObject());
		}
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		Comment1 comment = (Comment1) resource;
		User receiver = null;
		
		if(event.getAction() == EventType.Comment_Reply) {
			receiver = comment.getUser();
			users.add(receiver);
		}
		
		return users;
	}

	@Override
	User getSender() {
//		return event.getActor();
		return null;
	}

	@Override
	String getNotificationMessage() {
		//return ((Comment) resource).getText();
		//comment is not needed for notification
		return null;
	}
	
	@Override
	boolean isConditionMet(User sender, User receiver) {
		if (receiver != null && sender.getId() != receiver.getId()) {
			return true;
		} else {
//			logger.error("Commenting on the resource of a type: " + 
//					((Comment1) resource).getObject().getClass() + " is not captured.");
			return false;
		}
	}

}
