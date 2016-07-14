package org.prosolo.services.twitter;

import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
public interface TwitterApiManager {

	OauthAccessToken getOauthAccessToken(long userId);

	String getTwitterTokenUrl(long userId, String callbackUrl) throws TwitterException;

	OauthAccessToken verifyAndGetTwitterAccessToken(long userId, String oauthVerifier) throws TwitterException;

	Twitter getTwitter();

}
