/**
 * 
 */
package org.prosolo.web.settings.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;

/**
 * @author "Nikola Milikic"
 * 
 */
public class UserNotificationSettingsData implements Serializable {
	
	private static final long serialVersionUID = 451173971700565387L;

	private long id;
	
	private List<NotificationSettingsData> settings;
	
	public UserNotificationSettingsData(UserNotificationsSettings notificationsSettings) {
		this.id = notificationsSettings.getId();
		this.settings = new ArrayList<NotificationSettingsData>();
		
		for (NotificationSettings notificationSettingsData : notificationsSettings.getNotifications()) {
			this.settings.add(new NotificationSettingsData(notificationSettingsData));
		}
	}
	
	public long getId() {
		return id;
	}
	
	public List<NotificationSettingsData> getSettings() {
		return settings;
	}

}
