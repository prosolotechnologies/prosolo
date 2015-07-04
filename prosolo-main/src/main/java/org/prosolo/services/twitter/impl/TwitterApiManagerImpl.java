package org.prosolo.services.twitter.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.user.OauthAccessToken;
import org.prosolo.domainmodel.user.ServiceType;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.TwitterConfigurationManager;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
@Service("org.prosolo.services.twitter.TwitterApiManager")
public class TwitterApiManagerImpl implements TwitterApiManager {
	
	private static Logger logger = Logger.getLogger(TwitterApiManagerImpl.class);
	
	@Autowired UserOauthTokensManager oauthAccessTokenManager;
	@Autowired TwitterStreamsManager twitterStreamsManager;
	@Autowired TwitterConfigurationManager twitterConfigurationManager;

	private static Map<Long, RequestToken> requestTokens = null;
	
	public TwitterApiManagerImpl() throws TwitterException, IllegalArgumentException, IOException {
		if (requestTokens == null){
			requestTokens = new HashMap<Long, RequestToken>();
		}
	}
 
	@Override
	public OauthAccessToken verifyAndGetTwitterAccessToken(User user, String oauthVerifier) {
		OauthAccessToken oauthAccessToken = getOauthAccessToken(user);
		AccessToken accessToken = null;
		
		
		if (oauthAccessToken != null) {
			accessToken = new AccessToken(oauthAccessToken.getToken(), oauthAccessToken.getTokenSecret());
			this.getTwitter().setOAuthAccessToken(accessToken);
			
			try {
				this.getTwitter().verifyCredentials();
				} catch (TwitterException te) {
				oauthAccessTokenManager.deleteUserOauthAccessToken(user, ServiceType.TWITTER);
				accessToken = null;
			}
			return oauthAccessToken;
		}
		//retrieve the access token and delete the requestToken from the map. We don't need any more
		try {
			accessToken = this.getTwitter().getOAuthAccessToken(requestTokens.remove(user.getId()), oauthVerifier);
			String screenName = accessToken.getParameter("screen_name");
			long userId = accessToken.getUserId();
			String token = accessToken.getToken();
			String tokenSecret = accessToken.getTokenSecret();
			
			logger.debug("X token and token secret:" + token + " : "	+ tokenSecret);
			logger.debug("X screenName:"+screenName+" id:"+userId);
		
			oauthAccessToken = oauthAccessTokenManager.createOrUpdateOauthAccessToken(
					user, ServiceType.TWITTER, token,
					tokenSecret, screenName, "https://twitter.com/"+screenName, userId);
			
			if (oauthAccessToken!=null) {
				logger.debug("created access token:" +oauthAccessToken.getProfileLink());
				twitterStreamsManager.addNewTwitterUserAndRestartStream(userId);
			}
			this.getTwitter().setOAuthAccessToken(null);
		}catch (java.lang.IllegalStateException e) {
			//logger.error("IllegalStateException in checking twitter status for user"+e.getLocalizedMessage());
			throw e;
		} 
		
		catch (TwitterException e) {
			logger.error("There is no toke available",e);
		}
		
		return oauthAccessToken;
	}
	
	@Override
	public String getTwitterTokenUrl(User user, String callbackUrl)	throws TwitterException {
		// Add the requestToken for each user :)
		this.getTwitter().setOAuthAccessToken(null);
		RequestToken rt =  this.getTwitter().getOAuthRequestToken(callbackUrl);
		requestTokens.put(user.getId(), rt);
		 
		String authorizationURL = rt.getAuthorizationURL();
		return authorizationURL;
	}
	
	@Override
	public OauthAccessToken getOauthAccessToken(User user) throws IllegalAccessError {
		return oauthAccessTokenManager.getOauthAccessToken(user, ServiceType.TWITTER);
	}
	
	@Override
	public Twitter getTwitter() {
		return twitterConfigurationManager.getTwitterInstance();
	}
}
