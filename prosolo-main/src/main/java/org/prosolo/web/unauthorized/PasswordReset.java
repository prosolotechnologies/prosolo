package org.prosolo.web.unauthorized;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.apache.poi.hslf.record.Sound;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="passwordreset")
@Component("passwordreset")
@Scope("request")
public class PasswordReset implements Serializable {
	
	private static final long serialVersionUID = -4897709561117788195L;

	private static Logger logger = Logger.getLogger(PasswordReset.class);
	
	@Inject private UserManager userManager;
	@Inject private PasswordResetManager passwordResetManager;
	
	private String email;
	
	public void reset() {
		User user = userManager.getUser(email);
	
		if (user != null) {
			boolean resetLinkSent = passwordResetManager.initiatePasswordReset(user, email, CommonSettings.getInstance().config.appConfig.domain + "recovery");
			
			if (resetLinkSent) {
				PageUtil.fireSuccessfulInfoMessage("resetMessage", "Reset instructions have been sent to "+email);
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect("reset/successful/" + URLEncoder.encode(email, "utf-8"));
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
				PageUtil.fireErrorMessage("resetMessage", "Error reseting the password");
			}
		} else {
			try {
				PageUtil.fireErrorMessage("resetMessage", "Error", 
						ResourceBundleUtil.getMessage("passwordreset.noUser", new Locale("en"), email));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}  
		}
	}
	public void sendNewPassword() {
		
		User user = userManager.getUser(email);
		if (user != null) {
			boolean resetLinkSent = passwordResetManager.initiatePasswordReset(user, email, CommonSettings.getInstance().config.appConfig.domain + "recovery");
			
			if (resetLinkSent) {
				PageUtil.fireSuccessfulInfoMessage("resetMessage", "Password instructions have been sent to "+email);
				
			} else {
				PageUtil.fireErrorMessage("resetMessage", "Error sending password instruction");
			}
		} else {
			try {
				PageUtil.fireErrorMessage("resetMessage", "Error", 
						ResourceBundleUtil.getMessage("passwordreset.noUser", new Locale("en"), email));
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}  
		}
	}
	
	/*
	 *  GETTERS / SETTERS
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
