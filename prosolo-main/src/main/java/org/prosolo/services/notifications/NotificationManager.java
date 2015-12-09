package org.prosolo.services.notifications;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

public interface NotificationManager extends AbstractManager {

//	List<Notification> fetchNewNotifications(User user, int limit);

	List<Notification> getNotifications(User user, int page, int limit);

	List<Notification> getNotifications(User user, int limit, Collection<Notification> filterList);

	Notification markAsRead(Notification notification, Session session);
	
	boolean markAsRead(long[] notificationIds, Session session) throws ResourceCouldNotBeLoadedException;
	
	Notification createNotification(BaseEntity resource, User creator, User receiver, EventType type, 
			String message, Date date, boolean notifyByUI, 
			boolean notifyByEmail, Session session);

	Integer getNumberOfUnreadNotifications(User user);

	void markAsReadAllUnreadNotifications(User user, Session session);

	Notification markNotificationStatus(long notificationId, NotificationAction status) throws ResourceCouldNotBeLoadedException;

	boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String notificationType, String notificationShortType, String resourceTitle, String message, String date, boolean notifyByUI);
}
