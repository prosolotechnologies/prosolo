package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class AppConfig {

	@Element(name = "messages-inbox-refresh-rate")
	public int messagesInboxRefreshRate;

	@Element(name = "notifications-refresh-rate")
	public int notificationsRefreshRate;

	@Element(name = "password-reset-key-validity-hours")
	public int passwordResetKeyValidityHours;

	@Element(name = "notifications")
	public NotificationsConfig notifications;

	@Element(name = "manage-section")
	public ManageSectionConfig manageSection;

	@Element(name = "registration")
	public RegistrationConfig registration;

	@Element(name = "plugin-config")
	public PluginConfig pluginConfig;

	public RegistrationConfig getRegistration() {
		return registration;
	}

}
