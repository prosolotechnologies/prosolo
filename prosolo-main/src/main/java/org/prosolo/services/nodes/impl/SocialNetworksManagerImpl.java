package org.prosolo.services.nodes.impl;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.SocialNetworksData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service("org.prosolo.services.nodes.SocialNetworksManager")
public class SocialNetworksManagerImpl extends AbstractManagerImpl implements SocialNetworksManager {

	private static final long serialVersionUID = 1492068723251986359L;

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
	@Transactional(readOnly = false)
	public UserSocialNetworksData getSocialNetworksData(long userId) throws ResourceCouldNotBeLoadedException {
		UserSocialNetworksData result = null;
		try {
			UserSocialNetworks userSocialNetworks = getSocialNetworks(userId);
			Set<SocialNetworkAccount> socialNetworkAccounts = userSocialNetworks.getSocialNetworkAccounts();
			Set<SocialNetworkAccountData> listAccountsData = new HashSet<>();

			for (SocialNetworkAccount s : socialNetworkAccounts) {
				SocialNetworkAccountData socialNetworkAccountData = new SocialNetworkAccountData(s);
				listAccountsData.add(socialNetworkAccountData);
			}

			result = new UserSocialNetworksData(userSocialNetworks, listAccountsData);

		} catch (ResourceCouldNotBeLoadedException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link) {
		SocialNetworkAccount account = new SocialNetworkAccount();
		account.setSocialNetwork(name);
		account.setLink(link);
		return saveEntity(account);
	}

	@Override
	public void addSocialNetworkAccount(long userId, SocialNetworkName name, String link) throws ResourceCouldNotBeLoadedException {
		UserSocialNetworks socialNetworks = getSocialNetworks(userId);
		SocialNetworkAccount account = createSocialNetworkAccount(name, link);
		
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
	public SocialNetworkAccountData getSocialNetworkAccountData(long userId, SocialNetworkName socialNetworkName) {
		SocialNetworkAccount socialNetworkAccount = getSocialNetworkAccount(userId,socialNetworkName);

		if(socialNetworkAccount != null){
			return new SocialNetworkAccountData(socialNetworkAccount);
		}
		return null;
	}

	@Override
	public SocialNetworksData getSocialNetworkData(UserSocialNetworksData userSocialNetworksData) {

		Set<SocialNetworkAccountData> socialNetworkAccounts = userSocialNetworksData.getSocialNetworkAccounts();

		SocialNetworksData socialNetworksData = new SocialNetworksData();
		socialNetworksData.setId(userSocialNetworksData.getId());

		for (SocialNetworkAccountData account : socialNetworkAccounts) {
			socialNetworksData.setAccount(account);
		}
		return socialNetworksData;
	}

}
