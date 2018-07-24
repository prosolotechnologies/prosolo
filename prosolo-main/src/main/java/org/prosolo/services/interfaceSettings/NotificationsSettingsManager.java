/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.settings.data.NotificationSettingsData;

import java.util.List;

/**
 * @author "Nikola Milikic"
 *
 */
public interface NotificationsSettingsManager extends AbstractManager {

	List<NotificationSettings> getAllUserNotificationsSettings(long userId, Session session);

	NotificationSettings getNotificationSettings(long userId, NotificationType type);

	boolean shouldUserReceiveEmail(long userId, NotificationType type, Session session)
			throws DbConnectionException;

	List<NotificationSettingsData> getOrCreateNotificationsSettings(long userId);

	List<NotificationSettingsData> getOrCreateNotificationsSettings(long userId, Session session);

	void updateNotificationSettings(long userId, List<NotificationSettingsData> updatedNotificationSettings);

}
