package org.prosolo.services.notifications.factory;

import java.util.Locale;

import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

@Component
public class NotificationDataFactory {

	public NotificationData getNotificationData(Notification1 notification, String objectTitle,
			Locale locale) {
		NotificationData n = new NotificationData();
		n.setId(notification.getId());
		n.setRead(notification.isRead());
		n.setDate(DateUtil.getTimeAgoFromNow(notification.getDateCreated()));
		n.setNotificationType(notification.getType());
		UserData actor = new UserData(notification.getActor());
		n.setActor(actor);
		n.setLink(notification.getLink());
		n.setObjectType(notification.getObjectType());
		n.setObjectTitle(objectTitle != null ? objectTitle : "");
		
		if(locale != null) {
			setNotificationPredicateBasedOnLocale(n, locale);
		}
		
		return n;
	}
	
	public void setNotificationPredicateBasedOnLocale(NotificationData data, Locale locale) {
		try {
			String type = ResourceBundleUtil.getMessage(
					"notification.type." + data.getNotificationType().name(), 
					locale);
			String objectType = ResourceBundleUtil.getResourceType(data.getObjectType().name(), locale);
			data.setPredicate(type + " " + objectType);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
