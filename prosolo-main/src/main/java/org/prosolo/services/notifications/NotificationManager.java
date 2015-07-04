package org.prosolo.services.notifications;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.notifications.Notification;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;

public interface NotificationManager extends AbstractManager {

//	List<Notification> fetchNewNotifications(User user, int limit);

	List<Notification> getNotifications(User user, int page, int limit);

	List<Notification> getNotifications(User user, int limit, Collection<Notification> filterList);

	Notification markAsRead(Notification notification, Session session);
	
	boolean markAsRead(long[] notificationIds, Session session) throws ResourceCouldNotBeLoadedException;
	
	Notification createNotification(BaseEntity resource, User creator, User receiver, EventType type, String message, Date date, Session session);

	Integer getNumberOfUnreadNotifications(User user);

	void markAsReadAllUnreadNotifications(User user, Session session);

}
