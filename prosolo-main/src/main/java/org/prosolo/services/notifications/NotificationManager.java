package org.prosolo.services.notifications;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification1;
import org.prosolo.common.domainmodel.user.notifications.NotificationSection;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationData;
import org.prosolo.web.util.page.PageSection;

public interface NotificationManager extends AbstractManager {

	Integer getNumberOfUnreadNotifications(long userId, NotificationSection section);

	@Deprecated
	boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String notificationType, String notificationShortType, String resourceTitle, String message, String date, boolean notifyByUI);
	
	Notification1 createNotification(long actorId,
									 long receiverId, NotificationType type, Date date,
									 long objectId, ResourceType objectType, long targetId, ResourceType targetType, String link,
									 boolean notifyByEmail, boolean isObjectOwner, Session session, PageSection section) throws DbConnectionException;
	
	/**
	 * If all notifications for user need to be returned (no pagination) 0 should
	 * be passed for {@code limit} parameter
	 * @param userId
	 * @param page
	 * @param limit
	 * @param typesToInclude
	 * @param locale
	 * @return
	 * @throws DbConnectionException
	 */
	List<NotificationData> getNotificationsForUser(long userId, int page, int limit, 
			List<NotificationType> typesToInclude, Locale locale, NotificationSection section) throws DbConnectionException;
	
	NotificationData getNotificationData(long notificationId, boolean loadReceiver, Locale locale) 
			throws DbConnectionException;
	
	NotificationData getNotificationData(Notification1 notification, User receiver, 
			Session session, Locale locale) throws DbConnectionException;
	
	NotificationData getNotificationData(long notificationId, boolean loadReceiver, 
			Session session, Locale locale) throws DbConnectionException;
	
	boolean sendNotificationByEmail(String email, String receiverName, String actor, 
			String predicate, long objectId, ResourceType objectType, String objectTitle, String link, String date, NotificationType notificationType, Session session);
	
	int getNumberOfNotificationsForUser(long userId, List<NotificationType> types, NotificationSection section)
			throws DbConnectionException;

}
