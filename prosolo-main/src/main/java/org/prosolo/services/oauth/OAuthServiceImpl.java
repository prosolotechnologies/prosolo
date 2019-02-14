package org.prosolo.services.oauth;

import net.oauth.*;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.prosolo.services.oauth.exceptions.OauthException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.MessageDigest;

@Service("org.prosolo.services.oauth.OauthService")
public class OAuthServiceImpl implements OauthService, Serializable {

	private static Logger logger = Logger.getLogger(OAuthServiceImpl.class.getName());

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
			throw new OauthException("Error signing the message");
		}
	}
	
	
	@Override
	public void validatePostRequest(HttpServletRequest request, String url, String key, String secret) throws OauthException{
		try{
			OAuthMessage msg = OAuthServlet.getMessage(request, url);
			OAuthValidator validator = new SimpleOAuthValidator();
			OAuthConsumer consumer = new OAuthConsumer("about:blank", key, secret, null);
			validator.validateMessage(msg, new OAuthAccessor(consumer));
		} catch(Exception e) {
			logger.error("Error", e);
			throw new OauthException("Post request not valid");
		}
	}
	
	
}
