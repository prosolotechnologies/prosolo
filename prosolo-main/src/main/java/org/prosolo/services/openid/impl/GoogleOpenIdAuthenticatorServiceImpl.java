package org.prosolo.services.openid.impl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.openid.GoogleOpenIdAuthenticatorService;
import org.prosolo.services.openid.exception.OpenIdException;
import org.prosolo.web.openid.data.OpenIdUserInfo;
import org.prosolo.web.openid.provider.OpenIdProvider;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

@Service("org.prosolo.services.openid.GoogleOpenIdAuthentication")
public class GoogleOpenIdAuthenticatorServiceImpl implements GoogleOpenIdAuthenticatorService {

	private static Logger logger = Logger.getLogger(GoogleOpenIdAuthenticatorServiceImpl.class);

	private static String GOOGLE_CLIENT_ID = "62487549282-62skkvsr4n44of36su931161cps9pj5j.apps.googleusercontent.com";
	private static String GOOGLE_CLIENT_SECRET = "HTdqT0MuoMc9sP8Z5swfqZSG";
	private static String GOOGLE_REDIRECT_URL = "openid.xhtml";
	
	/**
	 * Setting state parameter as a session attribute for later security check
	 * and constructing and returning url to which request for retrieving user 
	 * info should be sent
	 */
	@Override
	public String startSignIn(HttpServletRequest request) {
		try {
			String redirectUrl = CommonSettings.getInstance().config.appConfig.domain + GOOGLE_REDIRECT_URL
					+ "?provider=" + OpenIdProvider.Google.name();
			String state = new BigInteger(130, new SecureRandom()).toString(32);
			
			request.getSession(true).setAttribute("state", state);
			return new GoogleAuthorizationCodeRequestUrl(GOOGLE_CLIENT_ID, redirectUrl,
					Arrays.asList("https://www.googleapis.com/auth/userinfo.email",
							"https://www.googleapis.com/auth/userinfo.profile")).setState(state).build();

		//	FacesContext.getCurrentInstance().getExternalContext().redirect(url);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return null;
		}

	}

	/**
	 * get code used for sending request for retrieving access token, 
	 * retrieve access token and finally getting user info using that
	 * access token
	 */
	@Override
	public OpenIdUserInfo completeSignIn(HttpServletRequest request) {
		try {
			String redirectUrl = CommonSettings.getInstance().config.appConfig.domain + GOOGLE_REDIRECT_URL + 
					"?provider="+OpenIdProvider.Google.name();
			JsonFactory jsonFactory = new JacksonFactory();
			HttpTransport httpTransport = new NetHttpTransport();
			
			String code = request.getParameter("code");
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
					GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, 
					code, redirectUrl).execute();
			
			GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
					.setTransport(httpTransport).setClientSecrets(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET).build()
					.setFromTokenResponse(tokenResponse);
	
			Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName("ProSolo")
					.build();
	
			//Tokeninfo tokenInfo = oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute();
			
			Userinfoplus info = oauth2.userinfo().get().execute();
			
			String validatedId = GoogleOAuthConstants.AUTHORIZATION_SERVER_URL + "/" + info.getId();
			
			return new OpenIdUserInfo(validatedId, info.getGivenName(), info.getFamilyName(), info.getEmail());
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public void validateCallbackRequest(HttpServletRequest request) throws OpenIdException {
		HttpSession session = request.getSession(false);
		String paramState = request.getParameter("state");
		if(session == null) {
			throw new OpenIdException("Session is null");
		}
		
		String sessionState = (String) session.getAttribute("state");
		session.removeAttribute("state");
		if (!paramState.equals(sessionState)) {
			   throw new OpenIdException("Invalid state parameter");
		}
	}
}
