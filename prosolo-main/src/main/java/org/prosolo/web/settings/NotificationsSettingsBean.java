/**
 * 
 */
package org.prosolo.web.settings;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.UserNotificationSettingsData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "notificationsSettingsBean")
@Component("notificationsSettingsBean")
@Scope("view")
public class NotificationsSettingsBean implements Serializable {

	private static final long serialVersionUID = 5647440616279979650L;

	protected static Logger logger = Logger.getLogger(NotificationsSettingsBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private NotificationsSettingsManager notificationsSettingsManager;
	
	private UserNotificationSettingsData notificationsSettings;
	
	@PostConstruct
	public void initialize() {
		if (loggedUser.getNotificationsSettings() != null)
			this.notificationsSettings = new UserNotificationSettingsData(loggedUser.getNotificationsSettings());
	}
	
	/*
	 * ACTIONS
	 */
	public void saveChanges() {
		notificationsSettingsManager.updateNotificationSettings(loggedUser.getNotificationsSettings(), this.notificationsSettings);
		
		PageUtil.fireSuccessfulInfoMessage(":notificationsSettingsForm:notificationsSettingsForm", "Notifications settings updated!");
	}

	
	/*
	 * GETTERS / SETTERS
	 */
	
	public UserNotificationSettingsData getNotificationsSettings() {
		return notificationsSettings;
	}

}
