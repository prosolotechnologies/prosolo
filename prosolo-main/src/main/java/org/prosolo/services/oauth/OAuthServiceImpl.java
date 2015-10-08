package org.prosolo.services.oauth;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.prosolo.services.oauth.exceptions.OauthException;
import org.springframework.stereotype.Service;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;

@Service("org.prosolo.services.oauth.OauthService")
public class OAuthServiceImpl implements OauthService, Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4171452705673100750L;

	
	@Override
	public String bodySignMessage(String msg, String key, String secret, String url) throws OauthException{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] msgBytes = msg.getBytes("utf-8");
			md.update(msgBytes);

			String hash64 = Base64.encodeBase64String(md.digest());
			OAuthConsumer oauthConsumer = new OAuthConsumer("about:blank", key, secret, null);
			OAuthAccessor oauthAccessor = new OAuthAccessor(oauthConsumer);
			OAuthMessage message = new OAuthMessage("POST", url, null, new ByteArrayInputStream(msg.getBytes("utf-8")));
			message.addParameter("oauth_body_hash", hash64);
			message.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
			message.addRequiredParameters(oauthAccessor);
			return message.getAuthorizationHeader(null);
		} catch (Exception e) {
			throw new OauthException("Error while signing the message");
		}
	}
	
	
	@Override
	public void validatePostRequest(HttpServletRequest request, String url, String key, String secret) throws OauthException{
		try{
			OAuthMessage msg = OAuthServlet.getMessage(request, url);
			OAuthValidator validator = new SimpleOAuthValidator();
			OAuthConsumer consumer = new OAuthConsumer("about:blank", key, secret, null);
			validator.validateMessage(msg, new OAuthAccessor(consumer));
		}catch(Exception e){
			throw new OauthException("Post request not valid");
		}
	}
	
	
}
