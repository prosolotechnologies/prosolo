package org.prosolo.services.notifications.factory;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

@Component
public class NotificationDataFactory {
	
	private static Logger logger = Logger.getLogger(NotificationDataFactory.class);

	public NotificationData getNotificationData(Notification1 notification, User receiver, 
			String objectTitle, String targetTitle, Locale locale) {
		NotificationData n = new NotificationData();
		n.setId(notification.getId());
		n.setRead(notification.isRead());
		n.setDate(notification.getDateCreated());
		n.setNotificationType(notification.getType());
		UserData actor = new UserData(notification.getActor());
		n.setActor(actor);
		
		if (receiver != null) {
			UserData rec = new UserData(receiver);
			n.setReceiver(rec);
		}
		
		n.setLink(notification.getLink());
		n.setObjectId(notification.getObjectId());
		n.setObjectType(notification.getObjectType());
		n.setObjectTitle(objectTitle != null ? objectTitle : "");
		
		n.setTargetId(notification.getTargetId());
		n.setTargetType(notification.getTargetType());
		n.setTargetTitle(targetTitle != null ? targetTitle : "");

		if (locale != null) {
			n.setPredicate(getNotificationPredicate(n.getNotificationType(), n.getObjectType(), locale));
			n.setRelationToTarget(getRelationToTarget(n.getNotificationType(), n.getTargetType(), locale));
		}
		
		n = typeBasedCorrections(n);
		
		return n;
	}
	
	public String getNotificationPredicate(NotificationType notificationType, ResourceType objectType, Locale locale) {
		String predicate = "";
		try {
			predicate += ResourceBundleUtil.getMessage(
					"notification.type." + notificationType.name(), 
					locale);
			
			if (objectType != null) {
				String objectTypeString = ResourceBundleUtil.getResourceType(objectType.name(), locale);
				
				predicate += " " + objectTypeString;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return predicate;
	}
	
	public String getRelationToTarget(NotificationType notificationType, ResourceType targetType, Locale locale) {
		String relationToTarget = "";
		try {
			relationToTarget += ResourceBundleUtil.getMessage(
					"notification.relationToTarget." + notificationType.name(), 
					locale);
			
			if (targetType != null) {
				String targetTypeString = ResourceBundleUtil.getResourceType(targetType.name(), locale);
				
				relationToTarget += " " + targetTypeString;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return relationToTarget;
	}
	
	private NotificationData typeBasedCorrections(NotificationData data) {
		switch (data.getNotificationType()) {
		case AnnouncementPublished:
			// do not display announcement title
			data.setObjectTitle("");
			break;
		default:
			break;
		}
		
		return data;
	}
}
