package org.prosolo.services.twitter.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
//import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.TwitterConfigurationManager;
import org.prosolo.services.twitter.TwitterSearchService;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

//@Service("org.prosolo.services.twitter.TwitterSearchService")
@Deprecated
public class TwitterSearchServiceImpl implements TwitterSearchService {

	private static Logger logger = Logger.getLogger(TwitterSearchServiceImpl.class);

	@Autowired private UserOauthTokensManager userOauthTokensManager;
	@Autowired TwitterConfigurationManager twitterConfigurationManager;

	public Twitter initializeTwitter(User user) {
		Twitter twitter = twitterConfigurationManager.getTwitterInstance();
		
		try {
			AccessToken accessToken = null;
			OauthAccessToken oAccessToken = userOauthTokensManager.getOauthAccessToken(user.getId(), ServiceType.TWITTER);

			if (oAccessToken == null) {
				return null;
			}
			
			accessToken = new AccessToken(oAccessToken.getToken(), oAccessToken.getTokenSecret());
			twitter.setOAuthAccessToken(accessToken);
		} catch (IllegalArgumentException e) {
			logger.error(e);
		}

		return twitter;
	}

}
