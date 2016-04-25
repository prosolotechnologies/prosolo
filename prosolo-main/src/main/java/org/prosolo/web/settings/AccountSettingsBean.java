/**
 * 
 */
package org.prosolo.web.settings;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.PageUtil;
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

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private UserManager userManager;
	
	private AccountData accountData;
	private String currentPassword;
	
	@PostConstruct
	public void initializeAccountData() {
		accountData = new AccountData();
		
		// emails
		String email = loggedUser.getUser().getEmail();
		accountData.setEmail(email);
	}
	
	/*
	 * ACTIONS
	 */
	public void savePassChange() {
		if (accountData.getPassword().equals(accountData.getPasswordConfirm())) {
			if (accountData.getPassword().length() < 6) {
				PageUtil.fireErrorMessage(":accountForm:accountFormGrowl", "Password is too short. It has to contain more that 6 characters.");
				return;
			}
			
			User user = userManager.changePassword(loggedUser.getUser(), accountData.getPassword());
			loggedUser.setUser(user);
			
			PageUtil.fireSuccessfulInfoMessage(":accountForm:accountFormGrowl", "Password updated!");
		} else {
			PageUtil.fireErrorMessage(":accountForm:accountFormGrowl", "Passwords do not match.");
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

}
