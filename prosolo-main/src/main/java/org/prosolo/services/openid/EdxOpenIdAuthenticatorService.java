package org.prosolo.services.openid;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.prosolo.web.openid.data.OpenIdUserInfo;

public interface EdxOpenIdAuthenticatorService {

	Map<String, Object> startSignIn(HttpServletRequest request, String returnToUrl);

	OpenIdUserInfo verifyResponse(HttpServletRequest httpReq);

}