package org.prosolo.web.unauthorized;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.authentication.exceptions.ResetKeyDoesNotExistException;
import org.prosolo.services.authentication.exceptions.ResetKeyExpiredException;
import org.prosolo.services.authentication.exceptions.ResetKeyInvalidatedException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "passwordrecovery")
@Component("passwordrecovery")
@Scope("view")
public class PasswordRecoveryBean {

	private static Logger logger = Logger.getLogger(PasswordRecoveryBean.class);
	
	@Autowired private PasswordResetManager passwordResetManager;
	@Autowired private UserManager userManager;
	@Autowired private AuthenticationService authenticationService;
	@Autowired private LoggedUserBean loggedUserBean;

	private String key;
	private boolean linkValid;
	private User user;
	private String newPass;
	private boolean toRedirect;
	
	public void login() {
		this.user = userManager.changePassword(user, newPass);
	
		// invalidate reset key
		passwordResetManager.invalidateResetKey(key);
		
		try {
			boolean loggedIn = authenticationService.login(user.getEmail().getAddress(), newPass);
			
			if (loggedIn) {
				loggedUserBean.init(user.getEmail().getAddress());
				this.toRedirect = true;
				PageUtil.fireInfoMessage("messages", "Password successfully changed. Redirecting...", "");
			}
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
	}
	
	public void checkResetKey() {
		
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		if (key != null) {
			this.key = key;
			try {
				User resetKeyOwner = passwordResetManager.checkIfKeyIsValid(key);
				
				if (resetKeyOwner != null) {
					linkValid = true;
					this.user = resetKeyOwner;
					
				} else {
					linkValid = false;
				}
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
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("login");
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public boolean isLinkValid() {
		return linkValid;
	}

	public void setLinkValid(boolean linkValid) {
		this.linkValid = linkValid;
	}

	public boolean isToRedirect() {
		return toRedirect;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
