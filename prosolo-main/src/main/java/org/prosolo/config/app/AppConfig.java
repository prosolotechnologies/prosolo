package org.prosolo.config.app;

import org.simpleframework.xml.Element;

public class AppConfig {

	@Element(name = "development-mode")
	public boolean developmentMode = false;

	@Element(name = "default-refresh-rate")
	public int defaultRefreshRate;

	@Element(name = "suggested-learning-refresh-rate")
	public int suggestedLearningRefreshRate;

	@Element(name = "messages-inbox-refresh-rate")
	public int messagesInboxRefreshRate;

	@Element(name = "notifications-refresh-rate")
	public int notificationsRefreshRate;

	@Element(name = "featured-news-refresh-rate")
	public int featuredNewsRefreshRate;

	@Element(name = "default-reindex-time")
	public int defaultReindexTime;

	@Element(name = "default-search-items-number")
	public int defaultSearchItemsNumber;

	@Element(name = "global-search-items-number")
	public int globalSearchItemsNumber;

	@Element(name = "default-likethis-items-number")
	public int defaultLikeThisItemsNumber;

	@Element(name = "default-sidebox-elements-number")
	public int defaultSideBoxElementsNumber;

	@Element(name = "password-reset-key-validity-hours")
	public int passwordResetKeyValidityHours;

	@Element(name = "profile")
	public ProfileConfig profile;

	@Element(name = "notifications")
	public NotificationsConfig notifications;

	@Element(name = "home")
	public HomeConfig home;

	@Element(name = "goals")
	public GoalPageConfig goals;

	@Element(name = "registration")
	public RegistrationConfig registration;

	@Element(name = "plugin-config")
	public PluginConfig pluginConfig;

	public boolean getDevelopmentMode() {
		return developmentMode;
	}

	public RegistrationConfig getRegistration() {
		return registration;
	}

}
