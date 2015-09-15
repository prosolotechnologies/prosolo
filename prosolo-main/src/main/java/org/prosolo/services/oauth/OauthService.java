package org.prosolo.services.oauth;

import javax.servlet.http.HttpServletRequest;

public interface OauthService {

	String bodySignMessage(String msg, String key, String secret, String url) throws Exception;

	void validatePostRequest(HttpServletRequest request, String url, String key, String secret) throws Exception;

}