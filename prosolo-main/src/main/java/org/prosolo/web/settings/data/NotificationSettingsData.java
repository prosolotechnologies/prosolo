package org.prosolo.web.settings.data;

import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;

import java.io.Serializable;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class NotificationSettingsData implements Serializable {
	
	private static final long serialVersionUID = 7178102238090170803L;

	private long userId;
	private NotificationType type;
	private boolean subscribedEmail;
	
	public NotificationSettingsData(NotificationSettings notificationSettings) {
		this.userId = notificationSettings.getUser().getId();
		this.type = notificationSettings.getType();
		this.subscribedEmail = notificationSettings.isSubscribedEmail();
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
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
