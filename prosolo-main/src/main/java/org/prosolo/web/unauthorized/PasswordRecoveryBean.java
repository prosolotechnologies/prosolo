package org.prosolo.web.unauthorized;

import java.io.IOException;
import java.net.URLEncoder;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.authentication.exceptions.ResetKeyDoesNotExistException;
import org.prosolo.services.authentication.exceptions.ResetKeyExpiredException;
import org.prosolo.services.authentication.exceptions.ResetKeyInvalidatedException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "passwordRecoveryBean")
@Component("passwordRecoveryBean")
@Scope("view")
public class PasswordRecoveryBean {

	private static Logger logger = Logger.getLogger(PasswordRecoveryBean.class);
	
	@Autowired private PasswordResetManager passwordResetManager;
	@Autowired private UserManager userManager;

	private String key;
	private boolean keyValid;
	private String newPass;
	
	public void init() {
		try {
			this.keyValid = passwordResetManager.checkIfResetKeyIsValid(key);
		} catch (ResetKeyDoesNotExistException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("resetMessage", "There is an error with your reset link. Please try again resetting the password.", null);  
		} catch (ResetKeyInvalidatedException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("resetMessage", "This reset link has probably been used already. Please try again resetting the password.", null);  
		} catch (ResetKeyExpiredException e) {
			logger.error(e.getMessage());
			PageUtil.fireErrorMessage("resetMessage", "This reset link has expired. Please try again resetting the password.", null);  
		}
	}
	
	public void saveNewPassword() {
		try {
			User user = passwordResetManager.getResetKeyUser(key);
			userManager.changePassword(user.getId(), newPass);
		
			// invalidate reset key
			passwordResetManager.invalidateResetKey(key);
		
//			boolean loggedIn = authenticationService.login(user.getEmail(), newPass);
//			
//			if (loggedIn) {
//				loggedUserBean.init(user.getEmail());
//				PageUtil.fireInfoMessage("messages", "Password successfully changed. Redirecting...", "");
//			}
			FacesContext.getCurrentInstance().getExternalContext().redirect("/login?success=" + URLEncoder.encode("Your password has been changed.", "utf-8"));
		} catch (ResourceCouldNotBeLoadedException | IOException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("There was an error reseting your password");
		}
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public boolean isKeyValid() {
		return keyValid;
	}

}
