package org.prosolo.web.settings.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class NotificationSettingsData implements Serializable {
	
	private static final long serialVersionUID = 7178102238090170803L;
	
	private NotificationType type;
	private boolean subscribedEmail;
	
	public NotificationSettingsData(NotificationSettings notificationSettings) {
		this.type = notificationSettings.getType();
		//this.subscribedUI = notificationSettings.isSubscribedUI();
		this.subscribedEmail = notificationSettings.isSubscribedEmail();
	}

	public NotificationType getType() {
		return type;
	}
	
	public void setType(NotificationType type) {
		this.type = type;
	}

	public boolean isSubscribedEmail() {
		return subscribedEmail;
	}

	public void setSubscribedEmail(boolean subscribedEmail) {
		this.subscribedEmail = subscribedEmail;
	}

}
