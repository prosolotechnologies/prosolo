package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class LikeEventProcessing extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(LikeEventProcessing.class);

	private BaseEntity resource;
	
	public LikeEventProcessing(Event event, Session session,
			NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager,
			UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		setResource();
	}

	private void setResource() {
		// TODO Auto-generated method stub
	}

	@Override
	List<Long> getReceiverIds() {
//		List<User> users = new ArrayList<>();
//
//		User receiver = null;
//		
//		if (resource instanceof SocialActivity) {
//			receiver = ((SocialActivity) resource).getMaker();
//			
//		} else if (resource instanceof TargetActivity) {
//			receiver = ((TargetActivity) resource).getMaker();
//		}
//		
//		users.add(receiver);
//		
//		return users;
		return null;
	}

	@Override
	long getSenderId() {
		return event.getActor().getId();
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		if (receiver != 0 && sender != receiver) {
			return true;
		} else {
			logger.error("Commenting on the resource of a type: " + 
					resource.getClass() + " is not captured.");
			return false;
		}
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Comment_Like;
	}

	@Override
	ObjectType getObjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	long getObjectId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	String getNotificationLink() {
		// TODO Auto-generated method stub
		return null;
	}

}
