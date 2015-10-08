package org.prosolo.services.oauth;

import javax.servlet.http.HttpServletRequest;

import org.prosolo.services.oauth.exceptions.OauthException;

public interface OauthService {

	String bodySignMessage(String msg, String key, String secret, String url) throws OauthException;

	void validatePostRequest(HttpServletRequest request, String url, String key, String secret) throws OauthException;

}