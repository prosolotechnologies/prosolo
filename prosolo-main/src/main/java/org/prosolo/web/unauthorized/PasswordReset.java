package org.prosolo.web.unauthorized;

import java.io.Serializable;
import java.util.Locale;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="passwordreset")
@Component("passwordreset")
@Scope("request")
public class PasswordReset implements Serializable {
	
	private static final long serialVersionUID = -4897709561117788195L;

	private static Logger logger = Logger.getLogger(PasswordReset.class);
	
	@Autowired private UserManager userManager;
	@Autowired private PasswordResetManager passwordResetManager;
	
	private String email;
	
	public void reset() {
		User user = userManager.getUser(email);
	
		if (user != null) {
			boolean resetLinkSent = passwordResetManager.resetPassword(user, email, Settings.getInstance().config.application.domain + "recovery.xhtml");
			
			if (resetLinkSent) {
				PageUtil.fireSuccessfulInfoMessage("resetMessage", "Reset instructions have ben sent to "+email);  
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
