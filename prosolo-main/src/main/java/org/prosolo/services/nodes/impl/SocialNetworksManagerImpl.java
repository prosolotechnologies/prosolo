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
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.SocialNetworksManager;
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
				"WHERE socialNetwork.user.id = :userId ";

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
			throws DbConnectionException, EventException {
		Result<SocialNetworkAccount> result = self.createSocialNetworkAccountAndGetEvents(name, link, contextData);

		for(EventData ev : result.getEvents()){
			eventFactory.generateEvent(ev);
		}
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<SocialNetworkAccount> createSocialNetworkAccountAndGetEvents(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException, EventException {
		SocialNetworkAccount account = new SocialNetworkAccount();
		account.setSocialNetwork(name);
		account.setLink(link);
		saveEntity(account);

		Result<SocialNetworkAccount> result = new Result<>();
		result.addEvent(eventFactory.generateEventData(EventType.UpdatedSocialNetworks, contextData,
				null, null, null, null));
		result.setResult(account);

		return result;
	}

	@Override
	public void addSocialNetworkAccount(long userId, SocialNetworkName name, String link, UserContextData contextData) throws ResourceCouldNotBeLoadedException, EventException {
		UserSocialNetworks socialNetworks = getSocialNetworks(userId);
		SocialNetworkAccount account = createSocialNetworkAccount(name, link, contextData);

		socialNetworks.addSocialNetworkAccount(account);
		saveEntity(socialNetworks);
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
	@Transactional(readOnly = false)
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
	public void saveUserSocialNetworks(UserSocialNetworks userSocialNetworks) throws DbConnectionException {
		saveEntity(userSocialNetworks);
	}

}
