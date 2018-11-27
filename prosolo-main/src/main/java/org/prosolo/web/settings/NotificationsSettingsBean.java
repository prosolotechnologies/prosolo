/**
 * 
 */
package org.prosolo.web.settings;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.NotificationSettingsData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.List;

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
	
	private List<NotificationSettingsData> notificationsSettings;

	public void initialize() {
		try {
			this.notificationsSettings = notificationsSettingsManager.getOrCreateNotificationsSettings(
					loggedUser.getUserId());
		} catch (DbConnectionException e) {
			logger.error("error", e);
			PageUtil.fireErrorMessage("Error loading the page");
		}
	}
	
	/*
	 * ACTIONS
	 */
	public void saveChanges() {
		notificationsSettingsManager.updateNotificationSettings(loggedUser.getUserId(), this.notificationsSettings);
		
		PageUtil.fireSuccessfulInfoMessage("Notifications settings have been updated");
	}

	
	/*
	 * GETTERS / SETTERS
	 */

	public List<NotificationSettingsData> getNotificationsSettings() {
		return notificationsSettings;
	}

	public void setNotificationsSettings(List<NotificationSettingsData> notificationsSettings) {
		this.notificationsSettings = notificationsSettings;
	}
}
