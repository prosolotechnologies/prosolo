package org.prosolo.services.twitter;

import org.prosolo.common.domainmodel.user.OauthAccessToken;
import org.prosolo.common.domainmodel.user.User;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author Zoran Jeremic 2013-08-04
 *
 */
public interface TwitterApiManager {

	OauthAccessToken getOauthAccessToken(User user);

	String getTwitterTokenUrl(User user, String callbackUrl) throws TwitterException;

	OauthAccessToken verifyAndGetTwitterAccessToken(User user, String oauthVerifier) throws TwitterException;

	Twitter getTwitter();

}
