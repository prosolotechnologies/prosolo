
package org.prosolo.services.twitter;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;

/**
 * @author "Nikola Milikic"
 *
 */
public interface UserOauthTokensManager {

	OauthAccessToken getOauthAccessToken(long userId, ServiceType serviceType);

	boolean hasOAuthAccessToken(long userId, ServiceType serviceType);

	OauthAccessToken createOrUpdateOauthAccessToken(long userId,
			ServiceType service, String token,
			String tokenSecret, String screenName, 
			String profileLink, long twitterUserId);

	OauthAccessToken createNewOauthAccessToken(long userId, ServiceType service,
			String token, String tokenSecret, String screenName,
			String profileLink, long twitterUserId);

	long deleteUserOauthAccessToken(long userId, ServiceType service);

	List<OauthAccessToken> getAllTwitterUsersTokens();

	User getUserByTwitterUserId(long userId);

	List<Long> getAllTwitterUsersTokensUserIds();

}