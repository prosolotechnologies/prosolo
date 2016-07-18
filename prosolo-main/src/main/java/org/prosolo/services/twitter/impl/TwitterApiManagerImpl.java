package org.prosolo.services.twitter.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.TwitterConfigurationManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
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
	
	@Inject 
	private UserOauthTokensManager oauthAccessTokenManager;
	@Inject 
	private AnalyticalServiceCollector analyticalServiceCollector;
	@Inject 
	private TwitterConfigurationManager twitterConfigurationManager;

	private static Map<Long, RequestToken> requestTokens = null;
	
	public TwitterApiManagerImpl() {
		requestTokens = new HashMap<Long, RequestToken>();
	}
 
	@Override
	public OauthAccessToken verifyAndGetTwitterAccessToken(long userId, String oauthVerifier) {
		OauthAccessToken oauthAccessToken = getOauthAccessToken(userId);
		AccessToken accessToken = null;
		
		// if there already is an oauth token, delete it
		if (oauthAccessToken != null) {
			accessToken = new AccessToken(oauthAccessToken.getToken(), oauthAccessToken.getTokenSecret());
			this.getTwitter().setOAuthAccessToken(accessToken);
			
			try {
				this.getTwitter().verifyCredentials();
			} catch (TwitterException te) {
				oauthAccessTokenManager.deleteUserOauthAccessToken(userId, ServiceType.TWITTER);
				accessToken = null;
			}
			return oauthAccessToken;
		}
		
		//retrieve the access token and delete the requestToken from the map. We don't need any more
		try {
			accessToken = this.getTwitter().getOAuthAccessToken(requestTokens.remove(userId), oauthVerifier);
			String screenName = accessToken.getParameter("screen_name");
			String token = accessToken.getToken();
			String tokenSecret = accessToken.getTokenSecret();
			
			logger.debug("X token and token secret:" + token + " : "	+ tokenSecret);
			logger.debug("X screenName:"+screenName+" id:"+userId);
			
			oauthAccessToken = oauthAccessTokenManager.createOrUpdateOauthAccessToken(
					userId, ServiceType.TWITTER, token,
					tokenSecret, screenName, "https://twitter.com/"+screenName, accessToken.getUserId());
//			oauthAccessToken = oauthAccessTokenManager.createOrUpdateOauthAccessToken(
//					userId, ServiceType.TWITTER, token,
//					tokenSecret, screenName, "https://twitter.com/"+screenName);
			
			if (oauthAccessToken!=null) {
				logger.debug("created access token:" +oauthAccessToken.getProfileLink());
				System.out.println("ADD NEW TWITTER USER :"+userId);
				//twitterStreamsManager.addNewTwitterUserAndRestartStream(userId);
				 analyticalServiceCollector.updateTwitterUser(userId,true);
			}
			this.getTwitter().setOAuthAccessToken(null);
		} catch (java.lang.IllegalStateException e) {
			//logger.error("IllegalStateException in checking twitter status for user"+e.getLocalizedMessage());
			throw e;
		} catch (TwitterException e) {
			logger.error("There is no toke available", e);
		}
		
		return oauthAccessToken;
	}
	
	@Override
	public String getTwitterTokenUrl(long userId, String callbackUrl) throws TwitterException {
		this.getTwitter().setOAuthAccessToken(null);
		RequestToken rt =  this.getTwitter().getOAuthRequestToken(callbackUrl);
		requestTokens.put(userId, rt);
		 
		return rt.getAuthorizationURL();
	}
	
	@Override
	public OauthAccessToken getOauthAccessToken(long userId) throws IllegalAccessError {
		return oauthAccessTokenManager.getOauthAccessToken(userId, ServiceType.TWITTER);
	}
	
	@Override
	public Twitter getTwitter() {
		return twitterConfigurationManager.getTwitterInstance();
	}
}
