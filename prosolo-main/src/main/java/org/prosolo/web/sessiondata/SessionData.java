package org.prosolo.web.sessiondata;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.services.activityWall.filters.Filter;

public class SessionData {

	private long userId;
	private long organizationId;
	private String encodedUserId;
	private String name;
	private String lastName;
	private String fullName;
	private String email;
	private String password;
	private String avatar;
	private String position;
	private boolean doNotShowTutorial;
	private Set<String> pagesTutorialPlayed = new HashSet<String>();
	private String ipAddress;

	private String sessionId;

	private Filter selectedStatusWallFilter;

	private UserSettings userSettings;
	private UserNotificationsSettings notificationsSettings;

	public String getSelectedRole() {
		System.out.println("GET SELECTED ROLE:"+selectedRole);
		return selectedRole;
	}

	public void setSelectedRole(String selectedRole) {
		System.out.println("Set selected role to:"+selectedRole);
		this.selectedRole = selectedRole;
	}

	private String selectedRole;
	
	
	/*
	 * GETTERS / SETTERS
	 */
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public String getEncodedUserId() {
		return encodedUserId;
	}

	public void setEncodedUserId(String encodedUserId) {
		this.encodedUserId = encodedUserId;
	}

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

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public UserSettings getInterfaceSettings() {
		return userSettings;
	}

	public Locale getLocale() {
		return userSettings.getLocaleSettings().createLocale();
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
}
