package org.prosolo.web.openid;

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.services.openid.EdxOpenIdAuthenticatorService;
import org.prosolo.web.openid.data.OpenIdUserInfo;

public class EdxOpenIdAuthenticator implements OpenIdAuthenticator {

	private static Logger logger = Logger.getLogger(GoogleOpenIdAuthenticator.class);

	private EdxOpenIdAuthenticatorService edxAuthenticator;

	public EdxOpenIdAuthenticator(EdxOpenIdAuthenticatorService edxAuthenticator) {
		this.edxAuthenticator = edxAuthenticator;
	}

	@Override
	public void startSignIn() {
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			Map<String, Object> result = edxAuthenticator.startSignIn(request);
			
			String url = (String) result.get("url");
			
			if (url != null) {
				boolean isVersionTwo = (boolean) result.get("isVersionTwo");
				if (isVersionTwo) {
					FacesContext.getCurrentInstance().getExternalContext().redirect(url);
				} else {
					ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
					context.redirect(url);
					FacesContext.getCurrentInstance().responseComplete();
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public OpenIdUserInfo completeSignIn() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		return edxAuthenticator.verifyResponse(request);
	}

}
