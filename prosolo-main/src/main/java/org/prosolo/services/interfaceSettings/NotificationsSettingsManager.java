/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.settings.data.UserNotificationSettingsData;

/**
 * @author "Nikola Milikic"
 *
 */
public interface NotificationsSettingsManager extends AbstractManager {

	UserNotificationsSettings getNotificationsSettings(long userId, Session session);

	NotificationSettings getEmailNotificationsSettings(long userId, NotificationType type);

	NotificationSettings getOrCreateNotificationSettings(long userId, NotificationType type, Session session)
			throws DbConnectionException;

	UserNotificationsSettings getOrCreateNotificationsSettings(long userId);
	
	UserNotificationsSettings getOrCreateNotificationsSettings(long userId, Session session);

	UserNotificationsSettings updateNotificationSettings(UserNotificationsSettings notificationsSettings, 
			UserNotificationSettingsData notificationsSettingsData);

}
