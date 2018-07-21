package org.prosolo.web.unauthorized;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.PasswordResetManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.net.URLEncoder;

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
					PageUtil.redirect("/reset/successful/" + URLEncoder.encode(email, "utf-8"));
				} catch (Exception e) {
					logger.error(e);
				}
			} else {
				PageUtil.fireErrorMessage("resetMessage", "Error reseting the password");
			}
		} else {
			PageUtil.fireErrorMessage("resetMessage", "Error",
					"There is no user registered with an email address "+email+". Please try again.");
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
