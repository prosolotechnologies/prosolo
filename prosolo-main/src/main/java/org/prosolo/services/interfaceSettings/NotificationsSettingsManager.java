/**
 * 
 */
package org.prosolo.services.interfaceSettings;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.settings.data.UserNotificationSettingsData;

/**
 * @author "Nikola Milikic"
 *
 */
public interface NotificationsSettingsManager extends AbstractManager {

	UserNotificationsSettings getNotificationsSettings(long userId);

	UserNotificationsSettings getOrCreateNotificationsSettings(User user);
	
	UserNotificationsSettings getOrCreateNotificationsSettings(User user, Session session);

	UserNotificationsSettings updateNotificationSettings(UserNotificationsSettings notificationsSettings, 
			UserNotificationSettingsData notificationsSettingsData);

}
