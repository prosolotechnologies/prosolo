package org.prosolo.web.openid;

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
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
			String redirectUrl = Settings.getInstance().config.application.domain + 
					"openid.xhtml?provider=Edx";
					//returnToUrl("/openid.xhtml?provider=" + OpenIdProvider.Edx.name())
			Map<String, Object> result = edxAuthenticator.startSignIn(request, redirectUrl);
			
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
	public OpenIdUserInfo finishSignIn() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		return edxAuthenticator.verifyResponse(request);

	}

	/**
	 * Create the current url and add another url path fragment on it. Obtain
	 * from the current context the url and add another url path fragment at the
	 * end
	 * 
	 * @param urlExtension
	 *            f.e. /nextside.xhtml
	 * @return the hole url including the new fragment
	 */
	private String returnToUrl(String urlExtension) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		String portExt = "";
		if (request.getServerPort() != 80) {
			portExt = ":" + request.getServerPort();
		}
		String returnToUrl = "http://" + request.getServerName() + portExt
				+ context.getApplication().getViewHandler().getActionURL(context, urlExtension);
		return returnToUrl;
	}

}
