package org.prosolo.services.twitter.impl;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.OauthAccessToken;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.TwitterConfigurationManager;
import org.prosolo.services.twitter.TwitterSearchService;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

@Service("org.prosolo.services.twitter.TwitterSearchService")
public class TwitterSearchServiceImpl implements TwitterSearchService {

	private static Logger logger = Logger.getLogger(TwitterSearchServiceImpl.class);

	@Autowired private DefaultManager defaultManager;
	@Autowired private TwitterApiManager twitterApiManager;
	@Autowired private UserOauthTokensManager userOauthTokensManager;
	@Autowired private TagManager tagManager;
	@Autowired TwitterConfigurationManager twitterConfigurationManager;

	public Twitter initializeTwitter(User user) {
		Twitter twitter = twitterConfigurationManager.getTwitterInstance();
		
		try {
			AccessToken accessToken = null;
			OauthAccessToken oAccessToken = userOauthTokensManager.findOauthAccessToken(user, ServiceType.TWITTER);

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
