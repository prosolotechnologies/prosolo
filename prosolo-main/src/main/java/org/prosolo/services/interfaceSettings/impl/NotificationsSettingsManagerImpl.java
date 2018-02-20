/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.web.settings.data.NotificationSettingsData;
import org.prosolo.web.settings.data.UserNotificationSettingsData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public UserNotificationsSettings getOrCreateNotificationsSettings(long userId) {
		return getOrCreateNotificationsSettings(userId, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserNotificationsSettings getOrCreateNotificationsSettings(long userId, Session session) 
		throws DbConnectionException {
		try {
			UserNotificationsSettings result = getNotificationsSettings(userId, session);
			
			if (result != null) {
				return result;
			} else {
				User user = (User) session.load(User.class, userId);
				UserNotificationsSettings notificationsSettings = new UserNotificationsSettings();
				notificationsSettings.setUser(user);
				notificationsSettings.setNotifications(getDefaultSubscribedEventTypes());
				
				session.saveOrUpdate(notificationsSettings);
				session.flush();
				
				return notificationsSettings;
			}
		} catch (ConstraintViolationException e) {
			session.clear();
			try {
				return getNotificationsSettings(userId, session);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new DbConnectionException("Error while retrieving notification settings");
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notification settings");
		}
	}
	
	@Override
	@Transactional
	public UserNotificationsSettings getNotificationsSettings(long userId, Session session) {
		String query =
			"SELECT settings " + 
			"FROM UserNotificationsSettings settings " + 
				"LEFT JOIN settings.user user " + 
			"WHERE user.id = :userId";
		
		UserNotificationsSettings result = (UserNotificationsSettings) session.createQuery(query)
				.setLong("userId", userId)
				.uniqueResult();
		return result;
	}
	
	@Override
	@Transactional (readOnly = false)
	public NotificationSettings getOrCreateNotificationSettings(long userId, NotificationType type, Session session) 
		throws DbConnectionException {
		try {
			NotificationSettings settings = getEmailNotificationsSettings(userId, type);
			
			if (settings != null) {
				return settings;
			} else {
				UserNotificationsSettings userNotificationsSettings = getOrCreateNotificationsSettings(userId, session);
				List<NotificationSettings> notifications = userNotificationsSettings.getNotifications();
				if(notifications != null) {
					for(NotificationSettings ns : userNotificationsSettings.getNotifications()) {
						if(ns.getType() == type) {
							return ns;
						}
					}
				}
				
				return null;
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving notification settings");
		}
	}
	
	@Override
	@Transactional
	public NotificationSettings getEmailNotificationsSettings(long userId, NotificationType type) {
		String query =
				"SELECT notifications " + 
				"FROM UserNotificationsSettings settings " + 
				"LEFT JOIN settings.notifications notifications " + 
				"LEFT JOIN settings.user user " + 
				"WHERE user.id = :userId " +
					"AND notifications.type = :type";
		
		NotificationSettings result = (NotificationSettings) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setString("type", type.name())
				.uniqueResult();
		return result;
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserNotificationsSettings updateNotificationSettings(UserNotificationsSettings notificationsSettings, UserNotificationSettingsData notificationsSettingsData) {
		Iterator<NotificationSettingsData> notDataIterator = notificationsSettingsData.getSettings().iterator();
		Iterator<NotificationSettings> notIterator = notificationsSettings.getNotifications().iterator();
		
		while (notDataIterator.hasNext()) {
			NotificationSettingsData notificationSettingsData = notDataIterator.next();
			NotificationSettings notificationSettings = notIterator.next();
			
			notificationSettings.setSubscribedEmail(notificationSettingsData.isSubscribedEmail());

			saveEntity(notificationSettings);
		}
		return saveEntity(notificationsSettings);
	}
	
	private List<NotificationSettings> getDefaultSubscribedEventTypes() {
		List<NotificationSettings> notificationTypes = new ArrayList<>();

		for (NotificationType notificationType : NotificationType.values()) {
			notificationTypes.add(new NotificationSettings(notificationType, true));
		}

		return notificationTypes;
	}

}
