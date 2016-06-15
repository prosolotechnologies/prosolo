package org.prosolo.services.notifications.factory;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

@Component
public class NotificationDataFactory {
	
	private static Logger logger = Logger.getLogger(NotificationDataFactory.class);

	public NotificationData getNotificationData(Notification1 notification, User receiver, 
			String objectTitle, Locale locale) {
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
		n.setObjectType(notification.getObjectType());
		n.setObjectTitle(objectTitle != null ? objectTitle : "");

		if (locale != null) {
			setNotificationPredicateBasedOnLocale(n, locale);
		}
		
		return n;
	}
	
	public void setNotificationPredicateBasedOnLocale(NotificationData data, Locale locale) {
		try {
			String predicate = "";
					
			predicate += ResourceBundleUtil.getMessage(
					"notification.type." + data.getNotificationType().name(), 
					locale);
			
			if (data.getObjectType() != null) {
				String objectType = ResourceBundleUtil.getResourceType(data.getObjectType().name(), locale);
				
				predicate += " " + objectType;
			}
			data.setPredicate(predicate);
		} catch(Exception e) {
			logger.error(e);
		}
	}
}
