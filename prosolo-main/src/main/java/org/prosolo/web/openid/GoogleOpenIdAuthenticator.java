package org.prosolo.web.openid;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.services.openid.GoogleOpenIdAuthenticatorService;
import org.prosolo.services.openid.exception.OpenIdException;
import org.prosolo.services.openid.impl.GoogleOpenIdAuthenticatorServiceImpl;
import org.prosolo.web.openid.data.OpenIdUserInfo;
import org.springframework.stereotype.Component;


public class GoogleOpenIdAuthenticator implements OpenIdAuthenticator {

	private static Logger logger = Logger.getLogger(GoogleOpenIdAuthenticator.class);
	
	private GoogleOpenIdAuthenticatorService googleAuthenticator;
	
	public GoogleOpenIdAuthenticator(GoogleOpenIdAuthenticatorService googleAuthenticator) {
		this.googleAuthenticator = googleAuthenticator;
	}
	
	@Override
	public void startSignIn() {
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			String url = googleAuthenticator.startSignIn(request);
			if(url != null) {
				FacesContext.getCurrentInstance().getExternalContext().redirect(url);
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}

	@Override
	public OpenIdUserInfo finishSignIn() {
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			googleAuthenticator.validateCallbackRequest(request);
			return googleAuthenticator.finishSignIn(request);
		} catch(OpenIdException e) {
			try {
				logger.error(e);
				FacesContext.getCurrentInstance().getExternalContext().redirect(Settings.getInstance().config.application.domain + "login?openiderr=Error while trying to login through your Google account");
			} catch (IOException e1) {	
				logger.error(e1);
			}
		}
		
		return null;
		
	}

}
