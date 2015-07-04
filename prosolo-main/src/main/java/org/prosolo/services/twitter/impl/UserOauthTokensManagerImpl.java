package org.prosolo.services.twitter.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.domainmodel.user.OauthAccessToken;
import org.prosolo.domainmodel.user.ServiceType;
import org.prosolo.domainmodel.user.User;
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
	public OauthAccessToken getOauthAccessToken(User user, ServiceType twitter) {
		if (user == null || twitter == null) {
			throw new IllegalArgumentException("Some arguments are null, can't continue with the action");
		}
		return findOauthAccessToken(user, twitter);
	}

	@Override
	@Transactional (readOnly = true)
	public OauthAccessToken findOauthAccessToken(User user, ServiceType serviceType) {
		String query = 
			"SELECT DISTINCT token " + "" +
			"FROM OauthAccessToken token " + 
			"WHERE token.user = :user";
		
		@SuppressWarnings("unchecked")
		
		List<OauthAccessToken> accessToken = persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.list();

		if (accessToken.isEmpty()) {
			return null;
		} else {
			return accessToken.get(0);
		}
	}

	@Override
	public OauthAccessToken createOrUpdateOauthAccessToken(User user,
			ServiceType service, String token,
			String tokenSecret, String screenName, 
			String profileLink, long userId) {
		
		OauthAccessToken oauthAccessToken = findOauthAccessToken(user, service);
		
		if (oauthAccessToken == null) {
			oauthAccessToken = createNewOauthAccessToken(user, service, token, tokenSecret,	screenName, profileLink, userId);
			return oauthAccessToken;
		}
		return oauthAccessToken;
	}

	@Override
	@Transactional
	public OauthAccessToken createNewOauthAccessToken(User user, ServiceType service,
			String token, String tokenSecret, String screenName,
			String profileLink, long userId) {
		OauthAccessToken oauthAccessToken = new OauthAccessToken();
		oauthAccessToken.setToken(token);
		oauthAccessToken.setService(service);
		oauthAccessToken.setTokenSecret(tokenSecret);
		oauthAccessToken.setUser(user);
		oauthAccessToken.setProfileName(screenName);
		oauthAccessToken.setProfileLink(profileLink);
		oauthAccessToken.setUserId(userId);
		persistence.currentManager().save(oauthAccessToken);
		persistence.currentManager().flush();
		return oauthAccessToken;
	}

	@Override
	@Transactional
	public void deleteUserOauthAccessToken(User user, ServiceType service) {
		OauthAccessToken token = findOauthAccessToken(user, service);
		
		if (token != null) {
			token=this.merge(token);
			Session session=persistence.currentManager();
			session.delete(token);
			session.flush();
		}
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
