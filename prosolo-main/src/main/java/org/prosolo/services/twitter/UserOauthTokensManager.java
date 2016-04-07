
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

	OauthAccessToken getOauthAccessToken(User user, ServiceType twitter);

	OauthAccessToken findOauthAccessToken(User user, ServiceType serviceType);

	OauthAccessToken createOrUpdateOauthAccessToken(User user, ServiceType service,
			String token, String tokenSecret, String screenName,
			String profileLink, long userId);

	OauthAccessToken createNewOauthAccessToken(User user, ServiceType service,
			String token, String tokenSecret, String screenName,
			String profileLink, long userId);

	long deleteUserOauthAccessToken(User user, ServiceType service);

	List<OauthAccessToken> getAllTwitterUsersTokens();

	User getUserByTwitterUserId(long userId);

	List<Long> getAllTwitterUsersTokensUserIds();

}