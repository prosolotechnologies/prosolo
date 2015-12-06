package org.prosolo.services.openid;

import javax.servlet.http.HttpServletRequest;

import org.prosolo.services.openid.exception.OpenIdException;
import org.prosolo.web.openid.data.OpenIdUserInfo;

public interface GoogleOpenIdAuthenticatorService {

	String startSignIn(HttpServletRequest request);

	OpenIdUserInfo finishSignIn(HttpServletRequest request);

	void validateCallbackRequest(HttpServletRequest request) throws OpenIdException;

}