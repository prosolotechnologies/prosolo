package org.prosolo.services.openid.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.prosolo.app.Settings;
import org.prosolo.services.openid.EdxOpenIdAuthenticatorService;
import org.prosolo.web.openid.data.OpenIdUserInfo;
import org.prosolo.web.openid.provider.OpenIdProvider;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.openid.EdxOpenIdAuthenticatorImpl")
public class EdxOpenIdAuthenticatorServiceImpl implements EdxOpenIdAuthenticatorService {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EdxOpenIdAuthenticatorServiceImpl.class);

	private final static String EDX_ENDPOINT = "https://courses.edx.org/openid/provider/login/";

	/**
	 * Create an authentication request. It performs a discovery on the
	 * user-supplied identifier. Attempt it to associate with the OpenID
	 * provider and retrieve one service endpoint for authentication. It adds
	 * some attributes for exchange on the AuthRequest. A List of all possible
	 * attributes can be found on @see http://www.axschema.org/types/
	 * 
	 */
	@Override
	public Map<String, Object> startSignIn(HttpServletRequest request) {
		try {
			String returnToUrl = Settings.getInstance().config.application.domain + 
					"openid.xhtml?provider="+ OpenIdProvider.Edx.name();
			ConsumerManager manager = new ConsumerManager();
			List discoveries = manager.discover(EDX_ENDPOINT);
			DiscoveryInformation discovered = manager.associate(discoveries);
		    HttpSession session = request.getSession(true);
		    session.setAttribute("manager", manager);
			session.setAttribute("discovered", discovered);
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

			FetchRequest fetch = FetchRequest.createFetchRequest();
			String version = discovered.getVersion();

			fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
			fetch.addAttribute("email", "http://axschema.org/contact/email", true);

			
			/*if (userSuppliedId.contains("edx.org")) {
				fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
				fetch.addAttribute("email", "http://axschema.org/contact/email", true);
			} else if (userSuppliedId.contains("myopenid")) {
				fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
				fetch.addAttribute("firstname", "http://schema.openid.net/firstname", true);
			} else {
				fetch.addAttribute("email", "http://axschema.org/contact/email", true);
				fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
				fetch.addAttribute("firstname", "http://axschema.org/namePerson/first", true);
				fetch.addAttribute("lastname", "http://axschema.org/namePerson/last", true);
				fetch.addAttribute("country", "http://axschema.org/contact/country/home", true);
				fetch.addAttribute("language", "http://axschema.org/pref/language", true);

				 ... 
			}*/
			
			authReq.addExtension(fetch);
			
			Map<String, Object> result = new HashMap<>();
			
			result.put("url", authReq.getDestinationUrl(true));
			result.put("isVersionTwo", discovered.isVersion2());
			
			return result;
			/*
			 * if (url != null) { if (!discovered.isVersion2()) {
			 * FacesContext.getCurrentInstance().getExternalContext().redirect(
			 * url); } else { ExternalContext context =
			 * FacesContext.getCurrentInstance().getExternalContext();
			 * context.redirect(url);
			 * FacesContext.getCurrentInstance().responseComplete(); } }
			 */
		} catch (OpenIDException e) {

			e.printStackTrace();
			logger.error(e);
			return null;
		}

	}
	
	/**
	 * Set the class members with date from the authentication response. Extract
	 * the parameters from the authentication response (which comes in as a HTTP
	 * request from the OpenID provider). Verify the response, examine the
	 * verification result and extract the verified identifier.
	 * 
	 * @param httpReq
	 *            httpRequest
	 * @return user info
	 */
	@Override
	public OpenIdUserInfo verifyResponse(HttpServletRequest httpReq) {
		try {
			HttpSession session = httpReq.getSession(false);
			ConsumerManager manager = (ConsumerManager) session.
					getAttribute("manager");
			DiscoveryInformation discovered = (DiscoveryInformation) session.
					getAttribute("discovered");
			
			session.removeAttribute("manager");
			session.removeAttribute("discovered");
			
			ParameterList response = new ParameterList(httpReq.getParameterMap());
			
			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0) {
				String param = httpReq.getQueryString();
				receivingURL.append("?").append(param);
			}
			logger.info("ReceivingURL:" + receivingURL.toString());
			logger.info("Response:" + response.toString());
			if(discovered!=null)
			logger.info("Discovered:" + discovered.toString());
			VerificationResult verification = manager.verify(receivingURL.toString(), response, discovered);

			Identifier verified = verification.getVerifiedId();
			if (verified != null) {
				AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

				FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
				String fullname = (String) fetchResp.getAttributeValues("ext0").get(0);
				String[] names = fullname.split(" ");
				
				OpenIdUserInfo user = new OpenIdUserInfo();
				
				if (names.length > 0)
					user.setFirstName(names[0]); 
				if (names.length > 1)
					user.setLastName(names[1]);
				user.setEmail((String) fetchResp.getAttributeValues("ext1").get(0));
				user.setId(verified.getIdentifier());
				return user;
			} else {
				logger.info("NOT VERIFIED USER:ReceivingURL:" + receivingURL.toString());
				logger.info("NOT VERIFIED USER:Response:" + response.toString());
				logger.info("NOT VERIFIED USER:Discovered:" + discovered.toString());
			}
		} catch (OpenIDException e) {
			logger.error("OpenIDException in OpenIDBean", e);
			// e.printStackTrace();
		}
		return null;
	}

}
