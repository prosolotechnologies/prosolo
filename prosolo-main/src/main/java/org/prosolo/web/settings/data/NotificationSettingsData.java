package org.prosolo.web.settings.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.interfacesettings.NotificationSettings;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class NotificationSettingsData implements Serializable {
	
	private static final long serialVersionUID = 7178102238090170803L;
	
	private EventType type;
	private boolean subscribedUI;
	private boolean subscribedEmail;
	
	public NotificationSettingsData(NotificationSettings notificationSettings) {
		this.type = notificationSettings.getType();
		this.subscribedUI = notificationSettings.isSubscribedUI();
		this.subscribedEmail = notificationSettings.isSubscribedEmail();
	}

	public EventType getType() {
		return type;
	}
	
	public void setType(EventType type) {
		this.type = type;
	}
	
	public boolean isSubscribedUI() {
		return subscribedUI;
	}

	public void setSubscribedUI(boolean subscribedUI) {
		this.subscribedUI = subscribedUI;
	}

	public boolean isSubscribedEmail() {
		return subscribedEmail;
	}

	public void setSubscribedEmail(boolean subscribedEmail) {
		this.subscribedEmail = subscribedEmail;
	}

}
