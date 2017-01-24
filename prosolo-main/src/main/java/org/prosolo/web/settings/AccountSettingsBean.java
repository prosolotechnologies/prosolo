package org.prosolo.web.settings;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 * 
 */
@ManagedBean(name = "accountSettings")
@Component("accountSettings")
@Scope("view")
public class AccountSettingsBean implements Serializable {

	private static final long serialVersionUID = 5647440616279979650L;

	protected static Logger logger = Logger.getLogger(AccountSettingsBean.class);

	@Autowired
	private LoggedUserBean loggedUser;
	@Autowired
	private UserManager userManager;

	private AccountData accountData;
	private String currentPassword;
	private User user;
	private String email;
	private long id;

	@Autowired
	private AuthenticationService authenticationService;

	@PostConstruct
	public void initializeAccountData() {
		accountData = new AccountData();

		// emails
		String email = loggedUser.getSessionData().getEmail();
		accountData.setEmail(email);
	}

	public void initializeAccountDataForPasswordChange() {
		accountData = new AccountData();
		user = userManager.getUser(email);
		accountData.setEmail(email);
	}
	/*
	 * ACTIONS
	 */
	public void savePassChange() {
		if(loggedUser.getPassword() != null && !loggedUser.getPassword().isEmpty()) {
			if (authenticationService.checkPassword(loggedUser.getSessionData().getPassword(), accountData.getPassword())) {
				savePasswordIfConditionsAreMet();
			} else {
				PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl", "Old password is not correct.");
			}
		} else {
			savePasswordIfConditionsAreMet();
		}
	}
	
	private void savePasswordIfConditionsAreMet() {
		if (accountData.getNewPassword().length() < 6) {
			PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl",
					"Password is too short. It has to contain more that 6 characters.");
			return;
		}

		try {
			String newPassEncrypted = userManager.changePassword(loggedUser.getUserId(), accountData.getNewPassword());
			loggedUser.getSessionData().setPassword(newPassEncrypted);
			
			PageUtil.fireSuccessfulInfoMessage(":settingsPasswordForm:settingsPasswordGrowl", "Password updated!");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl", "Error updating the password");
		}
	}
	
	public void savePassChangeForAnotherUser() {
		if(user.getPassword() != null && !user.getPassword().isEmpty()) {
			if (authenticationService.checkPassword(user.getPassword(), accountData.getPassword())) {
				savePasswordIfConditionsAreMetForAnotherUser();
			} else {
				PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl", "Old password is not correct.");
			}
		} else {
			savePasswordIfConditionsAreMetForAnotherUser();
		}
	}
	private void savePasswordIfConditionsAreMetForAnotherUser() {
		if (accountData.getNewPassword().length() < 6) {
			PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl",
					"Password is too short. It has to contain more that 6 characters.");
			return;
		}

		try {
			userManager.changePassword(user.getId(), accountData.getNewPassword());
			
			PageUtil.fireSuccessfulInfoMessage(":settingsPasswordForm:settingsPasswordGrowl", "Password updated!");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage(":settingsPasswordForm:settingsPasswordGrowl", "Error updating the password");
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public AccountData getAccountData() {
		return accountData;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
