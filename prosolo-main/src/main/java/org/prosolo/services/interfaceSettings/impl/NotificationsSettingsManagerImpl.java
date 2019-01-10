/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.web.settings.data.NotificationSettingsData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.NotificationsSettingsManager")
public class NotificationsSettingsManagerImpl extends AbstractManagerImpl implements NotificationsSettingsManager, Serializable {
	
	private static final long serialVersionUID = -7170043029317081775L;
	
	private static Logger logger = Logger.getLogger(InterfaceSettingsManager.class);

	@Override
	@Transactional (readOnly = false)
	public List<NotificationSettingsData> getOrCreateNotificationsSettings(long userId) {
		return getOrCreateNotificationsSettings(userId, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = false)
	public List<NotificationSettingsData> getOrCreateNotificationsSettings(long userId, Session session)
		throws DbConnectionException {
		try {
			List<NotificationSettings> result = getAllUserNotificationsSettings(userId, session);
			
			if (result == null || result.isEmpty()) {
				result = new LinkedList<>();

				User user = (User) session.load(User.class, userId);

				for (NotificationType notificationType : NotificationType.values()) {
					NotificationSettings settings = new NotificationSettings();
					settings.setUser(user);
					settings.setSubscribedEmail(true);
					settings.setType(notificationType);
					session.save(settings);

					result.add(settings);
				}
			}

			List<NotificationSettingsData> notificationSettingsData = new LinkedList<>();

			for (NotificationSettings settings : result) {
				notificationSettingsData.add(new NotificationSettingsData(settings));
			}

			return notificationSettingsData;
		} catch (Exception e) {
			logger.error("error", e);
			throw new DbConnectionException("Error while retrieving notification settings");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<NotificationSettings> getAllUserNotificationsSettings(long userId, Session session) {
		String query =
			"SELECT settings " + 
			"FROM NotificationSettings settings " +
			"WHERE settings.user.id = :userId";

		return session.createQuery(query)
				.setLong("userId", userId)
				.list();
	}
	
	@Override
	@Transactional (readOnly = false)
	public boolean shouldUserReceiveEmail(long userId, NotificationType type, Session session)
		throws DbConnectionException {

		try {
			NotificationSettings settings = getNotificationSettings(userId, type);
			
			if (settings != null) {
				return settings.isSubscribedEmail();
			} else {
				List<NotificationSettingsData> notificationSettingsData = getOrCreateNotificationsSettings(userId, session);

				Optional<NotificationSettingsData> settingsData = notificationSettingsData.stream().filter(s -> s.getType() == type).findFirst();

				if (settingsData.isPresent()) {
					return settingsData.get().isSubscribedEmail();
				}

				return false;
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notification settings");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public NotificationSettings getNotificationSettings(long userId, NotificationType type) {
		String query =
				"SELECT settings " +
				"FROM NotificationSettings settings " +
				"WHERE settings.user.id = :userId " +
					"AND settings.type = :type";
		
		NotificationSettings result = (NotificationSettings) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setString("type", type.name())
				.uniqueResult();
		return result;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateNotificationSettings(long userId, List<NotificationSettingsData> updatedNotificationSettings) {
		List<NotificationSettings> userNotificationSettings = getAllUserNotificationsSettings(userId, getPersistence().currentManager());

		for (NotificationSettings notificationSetting : userNotificationSettings) {
			NotificationSettingsData updatedNotificationSettingsData = updatedNotificationSettings.stream().filter(s -> s.getType() == notificationSetting.getType()).findFirst().get();

			if (notificationSetting.isSubscribedEmail() != updatedNotificationSettingsData.isSubscribedEmail()) {
				notificationSetting.setSubscribedEmail(updatedNotificationSettingsData.isSubscribedEmail());
				saveEntity(notificationSetting);
			}
		}
	}
	
}
