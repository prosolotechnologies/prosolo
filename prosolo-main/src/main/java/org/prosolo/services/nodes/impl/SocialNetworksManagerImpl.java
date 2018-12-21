package org.prosolo.services.nodes.impl;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service("org.prosolo.services.nodes.SocialNetworksManager")
public class SocialNetworksManagerImpl extends AbstractManagerImpl implements SocialNetworksManager {

	private static final long serialVersionUID = 1492068723251986359L;

	@Inject
	private EventFactory eventFactory;
	@Inject
	private SocialNetworksManager self;

	@Override
	@Transactional(readOnly = false)
	public UserSocialNetworks getSocialNetworks(long userId) throws ResourceCouldNotBeLoadedException {
		String query = 
				"SELECT socialNetwork " + 
				"FROM UserSocialNetworks socialNetwork " +
				"LEFT JOIN FETCH socialNetwork.user user " +
				"WHERE user.id = :userId ";

		UserSocialNetworks result = (UserSocialNetworks) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.uniqueResult();
		
		if (result != null) {
			return result;
		} else {
			UserSocialNetworks userSocialNetworks = new UserSocialNetworks();
			userSocialNetworks.setUser(loadResource(User.class, userId));
			return saveEntity(userSocialNetworks);
		}
	}

	@Override
	public SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException {
		Result<SocialNetworkAccount> result = self.createSocialNetworkAccountAndGetEvents(name, link, contextData);
		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
	}


	@Override
	@Transactional
	public Result<SocialNetworkAccount> createSocialNetworkAccountAndGetEvents(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException {
		SocialNetworkAccount account = new SocialNetworkAccount();
		UserSocialNetworks socialNetworks = null;
		try {
			socialNetworks = getSocialNetworks(contextData.getActorId());
		} catch (ResourceCouldNotBeLoadedException e) {
			e.printStackTrace();
		}
		account.setSocialNetwork(name);
		account.setLink(link);
		if (account != null) {
			socialNetworks.addSocialNetworkAccount(account);
		}
		saveEntity(account);
		saveEntity(socialNetworks);

		Result<SocialNetworkAccount> result = new Result<>();
		result.appendEvent(eventFactory.generateEventData(EventType.UpdatedSocialNetworks, contextData,
				null, null, null, null));
		result.setResult(account);

		return result;
	}

	@Transactional
	public UserSocialNetworksData getUserSocialNetworkData(long userId) {
		try {
			UserSocialNetworks userSocialNetworks = getSocialNetworks(userId);

			UserSocialNetworksData userSocialNetworksData = new UserSocialNetworksData();
			userSocialNetworksData.setId(userSocialNetworks.getId());
			userSocialNetworksData.setUserId(userId);

			for (SocialNetworkAccount account : userSocialNetworks.getSocialNetworkAccounts()) {
				userSocialNetworksData.setAccount(account);
			}
			return userSocialNetworksData;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading user social network data");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void updateSocialNetworkAccount(SocialNetworkAccount account, String newLink)
			throws DbConnectionException {
		try {
			account.setLink(newLink);
			saveEntity(account);
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException();
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateSocialNetworkAccount(long id, String newLink)
			throws DbConnectionException, ResourceCouldNotBeLoadedException {
		SocialNetworkAccount account = loadResource(SocialNetworkAccount.class, id);
		account.setLink(newLink);
		try {
			saveEntity(account);
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public SocialNetworkAccount getSocialNetworkAccount(long userId, SocialNetworkName socialNetworkName) {
		String query = 
				"SELECT accounts " + 
				"FROM UserSocialNetworks socialNetworks " +
				"LEFT JOIN socialNetworks.socialNetworkAccounts accounts " +
				"WHERE socialNetworks.user.id = :userId " + 
					"AND accounts.socialNetwork = :socialNetworkName";

		SocialNetworkAccount result = (SocialNetworkAccount) persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setString("socialNetworkName", socialNetworkName.name())
				.uniqueResult();
		
		if (result != null) {
			return result;
		}
		return null;
	}

	@Override
	@Transactional
	public SocialNetworkAccountData getSocialNetworkAccountData(long userId, SocialNetworkName socialNetworkName) {
		SocialNetworkAccount socialNetworkAccount = getSocialNetworkAccount(userId, socialNetworkName);

		if (socialNetworkAccount != null) {
			return new SocialNetworkAccountData(socialNetworkAccount);
		}
		return null;
	}

}
