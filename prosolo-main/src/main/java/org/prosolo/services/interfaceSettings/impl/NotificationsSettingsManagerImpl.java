/**
 * 
 */
package org.prosolo.services.interfaceSettings.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
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

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.interfaceSettings.NotificationsSettingsManager")
public class NotificationsSettingsManagerImpl extends AbstractManagerImpl implements NotificationsSettingsManager, Serializable {
	
	private static final long serialVersionUID = -7170043029317081775L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(InterfaceSettingsManager.class);

	@Override
	@Transactional (readOnly = false)
	public UserNotificationsSettings getOrCreateNotificationsSettings(long userId) {
		return getOrCreateNotificationsSettings(userId, persistence.currentManager());
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserNotificationsSettings getOrCreateNotificationsSettings(long userId, Session session) {
		User user = (User) session.load(User.class, userId);
		UserNotificationsSettings result = getNotificationsSettings(userId);
		
		if (result != null) {
			return result;
		} else {
			UserNotificationsSettings notificationsSettings = new UserNotificationsSettings();
			notificationsSettings.setUser(user);
			
			notificationsSettings.setNotificationsSettings(getDefaultSubscribedEventTypes());
			
			//this.persistence.save(notificationsSettings);
			session.saveOrUpdate(notificationsSettings);
			session.flush();
			
			return notificationsSettings;
		}
	}
	
	@Override
	@Transactional
	public UserNotificationsSettings getNotificationsSettings(long userId) {
		String query = 
			"SELECT settings " + 
			"FROM UserNotificationsSettings settings " + 
				"LEFT JOIN settings.user user " + 
			"WHERE user.id = :userId";
		
		UserNotificationsSettings result = (UserNotificationsSettings) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.uniqueResult();
		return result;
	}
	
	@Override
	@Transactional (readOnly = false)
	public UserNotificationsSettings updateNotificationSettings(UserNotificationsSettings notificationsSettings, UserNotificationSettingsData notificationsSettingsData) {
		Iterator<NotificationSettingsData> notDataIterator = notificationsSettingsData.getSettings().iterator();
		Iterator<NotificationSettings> notIterator = notificationsSettings.getNotificationsSettings().iterator();
		
		while (notDataIterator.hasNext()) {
			NotificationSettingsData notificationSettingsData = (NotificationSettingsData) notDataIterator.next();
			NotificationSettings notificationSettings = (NotificationSettings) notIterator.next();
			
			notificationSettings.setSubscribedEmail(notificationSettingsData.isSubscribedEmail());
			//notificationSettings.setSubscribedUI(notificationSettingsData.isSubscribedUI());
			
			saveEntity(notificationSettings);
		}
		return saveEntity(notificationsSettings);
	}
	
	private List<NotificationSettings> getDefaultSubscribedEventTypes() {
		List<NotificationSettings> notificationTypes = new ArrayList<NotificationSettings>();
		
		notificationTypes.add(new NotificationSettings(NotificationType.Follow_User, true));
		notificationTypes.add(new NotificationSettings(NotificationType.Assessment_Given, true));
		notificationTypes.add(new NotificationSettings(NotificationType.Comment, true));
		notificationTypes.add(new NotificationSettings(NotificationType.Comment_Like, true));
		notificationTypes.add(new NotificationSettings(NotificationType.Mention, true));
		
		return notificationTypes;
	}
	
//	private List<NotificationSettings> getDefaultSubscribedEventTypes() {
//		List<NotificationSettings> eventTypes = new ArrayList<NotificationSettings>();
//		
//		eventTypes.add(new NotificationSettings(EventType.JOIN_GOAL_REQUEST, true, true));
//		eventTypes.add(new NotificationSettings(EventType.JOIN_GOAL_REQUEST_APPROVED, true, true));
//		eventTypes.add(new NotificationSettings(EventType.JOIN_GOAL_REQUEST_DENIED, true, true));
//		eventTypes.add(new NotificationSettings(EventType.JOIN_GOAL_INVITATION, true, true));
//		eventTypes.add(new NotificationSettings(EventType.JOIN_GOAL_INVITATION_ACCEPTED, true, true));
//		eventTypes.add(new NotificationSettings(EventType.EVALUATION_REQUEST, true, true));
//		eventTypes.add(new NotificationSettings(EventType.EVALUATION_ACCEPTED, true, true));
//		eventTypes.add(new NotificationSettings(EventType.EVALUATION_GIVEN, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Follow, true, true));
//		eventTypes.add(new NotificationSettings(EventType.ACTIVITY_REPORT_AVAILABLE, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Comment, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Comment_Reply, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Like, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Dislike, true, true));
//		eventTypes.add(new NotificationSettings(EventType.Post, true, true));
//		
//		return eventTypes;
//	}
	
}
