package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

public class CommentEventProcessing extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(CommentEventProcessing.class);

	public CommentEventProcessing(Event event, Session session,
			NotificationManager notificationManager, 
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	protected void setResource() {
		this.resource = (Comment) session.merge(event.getObject());
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		Comment comment = (Comment) resource;
		BaseEntity commentedResource = comment.getObject();
		User receiver = null;
		
		if (commentedResource instanceof SocialActivity) {
			receiver = ((SocialActivity) comment.getObject()).getMaker();
			
		} else if (commentedResource instanceof TargetActivity) {
			receiver = ((TargetActivity) comment.getObject()).getMaker();
		}
		users.add(receiver);
		
		return users;
	}

	@Override
	User getSender() {
		return event.getActor();
	}

	@Override
	String getNotificationMessage() {
		return ((Comment) resource).getText();
	}

	@Override
	void afterProcessing(Notification notification, Session session) {
		// no processing needed
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		if (receiver != null && sender.getId() != receiver.getId()) {
			return true;
		} else {
			logger.error("Commenting on the resource of a type: " + 
					((Comment) resource).getObject().getClass() + " is not captured.");
			return false;
		}
	}

}
