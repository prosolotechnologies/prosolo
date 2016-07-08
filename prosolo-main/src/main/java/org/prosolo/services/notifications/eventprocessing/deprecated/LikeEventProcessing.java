package org.prosolo.services.notifications.eventprocessing.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;

@Deprecated
public class LikeEventProcessing extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(LikeEventProcessing.class);

	public LikeEventProcessing(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager) {
		super(event, session, notificationManager, notificationsSettingsManager);
	}

	@Override
	List<User> getReceivers() {
		List<User> users = new ArrayList<>();

		User receiver = null;
		
		if (resource instanceof SocialActivity) {
			receiver = ((SocialActivity) resource).getMaker();
			
		} else if (resource instanceof TargetActivity) {
			receiver = ((TargetActivity) resource).getMaker();
		}
		
		users.add(receiver);
		
		return users;
	}

	@Override
	User getSender() {
//		return event.getActor();
		return null;
	}

	@Override
	String getNotificationMessage() {
		return null;
	}

	@Override
	boolean isConditionMet(User sender, User receiver) {
		if (receiver != null && sender.getId() != receiver.getId()) {
			return true;
		} else {
			logger.error("Commenting on the resource of a type: " + 
					resource.getClass() + " is not captured.");
			return false;
		}
	}

}
