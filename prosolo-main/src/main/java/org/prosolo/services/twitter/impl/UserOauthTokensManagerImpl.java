package org.prosolo.services.twitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-08-04
 * 
 */
@Service("org.prosolo.services.twitter.UserOauthTokensManager")
@Transactional
public class UserOauthTokensManagerImpl extends AbstractManagerImpl implements UserOauthTokensManager {

	private static final long serialVersionUID = 6134989123256378614L;
	private static Logger logger = Logger.getLogger(OauthAccessToken.class);

	@Override
	public OauthAccessToken getOauthAccessToken(long userId, ServiceType twitter) {
		if (userId == 0) {
			throw new IllegalArgumentException("Some arguments are null, can't continue with the action");
		}
		return findOauthAccessToken(userId, twitter);
	}

	@Override
	@Transactional (readOnly = true)
	public OauthAccessToken findOauthAccessToken(long userId, ServiceType serviceType) {
		String query = 
			"SELECT DISTINCT token " + "" +
			"FROM OauthAccessToken token " + 
			"WHERE token.user.id = :userId " +
				"AND token.service = :serviceType";
		
		@SuppressWarnings("unchecked")
		List<OauthAccessToken> accessToken = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setString("serviceType", serviceType.name())
				.list();

		if (!accessToken.isEmpty()) {
			return accessToken.get(0);
		}
		return null;
	}

	@Override
	public OauthAccessToken createOrUpdateOauthAccessToken(long userId,
			ServiceType service, String token,
			String tokenSecret, String screenName, 
			String profileLink) {
		
		OauthAccessToken oauthAccessToken = findOauthAccessToken(userId, service);
		
		if (oauthAccessToken == null) {
			oauthAccessToken = createNewOauthAccessToken(userId, service, token, tokenSecret, screenName, profileLink);
			return oauthAccessToken;
		}
		return oauthAccessToken;
	}

	@Override
	@Transactional
	public OauthAccessToken createNewOauthAccessToken(long userId, ServiceType service,
			String token, String tokenSecret, String screenName,
			String profileLink) {
		OauthAccessToken oauthAccessToken = new OauthAccessToken();
		oauthAccessToken.setToken(token);
		oauthAccessToken.setService(service);
		oauthAccessToken.setTokenSecret(tokenSecret);
		oauthAccessToken.setUser(new User(userId));
		oauthAccessToken.setProfileName(screenName);
		oauthAccessToken.setProfileLink(profileLink);
		oauthAccessToken.setUserId(userId);
		persistence.currentManager().save(oauthAccessToken);
		persistence.currentManager().flush();
		return oauthAccessToken;
	}

	@Override
	@Transactional
	public long deleteUserOauthAccessToken(long userId, ServiceType service) {
		OauthAccessToken token = findOauthAccessToken(userId, service);
		long deletedUserId = 0;
		
		if (token != null) {
			token = this.merge(token);
			deletedUserId=token.getUserId();
			System.out.println("Deleting user:"+token.getUserId());
			Session session = persistence.currentManager();
			session.delete(token);
			session.flush();
		}
		return deletedUserId;
	}

	@Override
	@Transactional (readOnly = true)
	public List<OauthAccessToken> getAllTwitterUsersTokens() {
		String query = 
				"SELECT userToken " + 
				"FROM OauthAccessToken userToken ";
		
		logger.debug("hb query:" + query);
		
		@SuppressWarnings("unchecked")
		List<OauthAccessToken> result = persistence.currentManager().createQuery(query)
				.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<OauthAccessToken>();
	}
	@Override
	@Transactional (readOnly = true)
	public List<Long> getAllTwitterUsersTokensUserIds() {
		String query = 
				"SELECT userToken.userId " + 
				"FROM OauthAccessToken userToken ";
		
		logger.debug("hb query:" + query);
		
		@SuppressWarnings("unchecked")
		List<Long> result = persistence.currentManager().createQuery(query)
				.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<Long>();
	}

	@Override
	@Transactional (readOnly = true)
	public User getUserByTwitterUserId(long userId) {
		String query = 
				"SELECT user " + 
				"FROM OauthAccessToken userToken " + 
				"WHERE userToken.userId=:userId";
		
		logger.debug("hb query:" + query);

		return (User) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.uniqueResult();
	}

}
