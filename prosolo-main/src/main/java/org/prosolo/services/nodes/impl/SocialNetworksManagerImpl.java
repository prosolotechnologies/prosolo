package org.prosolo.services.nodes.impl;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.portfolio.data.SocialNetworkAccountData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.SocialNetworksManager")
public class SocialNetworksManagerImpl extends AbstractManagerImpl implements SocialNetworksManager {

	private static final long serialVersionUID = 1492068723251986359L;

	@Override
	@Transactional(readOnly = false)
	public UserSocialNetworks getSocialNetworks(long id) {
		String query = "SELECT socialNetwork " + "FROM UserSocialNetworks socialNetwork "
				+ "WHERE socialNetwork.id = :id ";

		return (UserSocialNetworks) persistence.currentManager().createQuery(query).setLong("id", id).uniqueResult();
	}

	@Override
	@Transactional(readOnly = false)
	public UserSocialNetworks getSocialNetworks(User user) {
		String query = "SELECT socialNetwork " + "FROM UserSocialNetworks socialNetwork "
				+ "WHERE socialNetwork.user = :user ";

		UserSocialNetworks result = (UserSocialNetworks) persistence.currentManager().createQuery(query)
				.setEntity("user", user).uniqueResult();

		if (result != null) {
			return result;
		} else {
			UserSocialNetworks userSocialNetworks = new UserSocialNetworks();
			userSocialNetworks.setUser(user);
			return saveEntity(userSocialNetworks);
		}
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
	@Transactional(readOnly = false)
	public void updateSocialNetworkAccount(SocialNetworkAccountData socialNetowrkAccountData)
			throws DbConnectionException {
		SocialNetworkAccount account = new SocialNetworkAccount();
		account.setId(socialNetowrkAccountData.getId());
		account.setLink(socialNetowrkAccountData.getLinkEdit());
		account.setSocialNetwork(socialNetowrkAccountData.getSocialNetworkName());
		try {
			saveEntity(account);
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}

	}

}
