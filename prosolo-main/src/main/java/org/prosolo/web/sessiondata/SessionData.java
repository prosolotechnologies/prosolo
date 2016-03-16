package org.prosolo.web.sessiondata;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.LearningGoalFilter;

public class SessionData {

	private User user;
	private String email;
	private String password;
	private String bigAvatar;
	private boolean doNotShowTutorial;
	private Set<String> pagesTutorialPlayed = new HashSet<String>();
	private String ipAddress;

	private Filter selectedStatusWallFilter;
	private LearningGoalFilter selectedLearningGoalFilter;

	private UserSettings userSettings;
	private UserNotificationsSettings notificationsSettings;
	
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return getUser().getName();
	}

	public String getLastName() {
		return getUser().getLastname();
	}

	public UserSettings getInterfaceSettings() {
		return userSettings;
	}

	public Locale getLocale() {
		return userSettings.getLocaleSettings().createLocale();
	}

	public String getBigAvatar() {
		return bigAvatar;
	}

	public void setBigAvatar(String bigAvatar) {
		this.bigAvatar = bigAvatar;
	}

	public boolean isDoNotShowTutorial() {
		return doNotShowTutorial;
	}

	public void setDoNotShowTutorial(boolean doNotShowTutorial) {
		this.doNotShowTutorial = doNotShowTutorial;
	}

	public Filter getSelectedStatusWallFilter() {
		return selectedStatusWallFilter;
	}

	public void setSelectedStatusWallFilter(Filter selectedStatusWallFilter) {
		this.selectedStatusWallFilter = selectedStatusWallFilter;
	}

	public LearningGoalFilter getSelectedLearningGoalFilter() {
		return selectedLearningGoalFilter;
	}

	public void setSelectedLearningGoalFilter(LearningGoalFilter selectedLearningGoalFilter) {
		this.selectedLearningGoalFilter = selectedLearningGoalFilter;
	}

	public UserNotificationsSettings getNotificationsSettings() {
		return notificationsSettings;
	}

	public void setNotificationsSettings(UserNotificationsSettings notificationsSettings) {
		this.notificationsSettings = notificationsSettings;
	}

	public Set<String> getPagesTutorialPlayed() {
		return pagesTutorialPlayed;
	}

	public void setPagesTutorialPlayed(Set<String> pagesTutorialPlayed) {
		this.pagesTutorialPlayed = pagesTutorialPlayed;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public UserSettings getUserSettings() {
		return userSettings;
	}

	public void setUserSettings(UserSettings userSettings) {
		this.userSettings = userSettings;
	}
}
