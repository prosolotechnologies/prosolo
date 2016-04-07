package org.prosolo.services.nodes.impl;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.SocialNetworksManager")
public class SocialNetworksManagerImpl extends AbstractManagerImpl implements SocialNetworksManager {

	private static final long serialVersionUID = 1492068723251986359L;
	
	@Override
	@Transactional (readOnly = false)
	public UserSocialNetworks getSocialNetworks(long id) {
		String query = 
			"SELECT socialNetwork " +
			"FROM UserSocialNetworks socialNetwork " +
			"WHERE socialNetwork.id = :id ";
		
		return (UserSocialNetworks) persistence.currentManager().createQuery(query).
				setLong("id", id).
				uniqueResult();
	}

	@Override
	@Transactional (readOnly = false)
	public UserSocialNetworks getSocialNetworks(User user) {
		String query = 
			"SELECT socialNetwork " +
			"FROM UserSocialNetworks socialNetwork " +
			"WHERE socialNetwork.user = :user ";
		
		UserSocialNetworks result = (UserSocialNetworks) persistence.currentManager().createQuery(query).
			setEntity("user", user).
			uniqueResult();
		
		if (result != null) {
			return result;
		} else {
			UserSocialNetworks userSocialNetworks = new UserSocialNetworks();
			userSocialNetworks.setUser(user);
			return saveEntity(userSocialNetworks);
		}
	}

	@Override
	@Transactional (readOnly = false)
	public UserSocialNetworks updateSocialNetwork(UserSocialNetworks userSocialNetworks,
			SocialNetworkName name, String link) {
		
		if (userSocialNetworks != null) {
			
			for (SocialNetworkAccount sn : userSocialNetworks.getSocialNetworkAccounts()) {
				if (sn.getSocialNetwork().equals(name)) {
					if (sn.getLink() == null || !sn.getLink().equals(link)) {
						sn.setLink(link);
						
						saveEntity(sn);
					} 
					return userSocialNetworks;
				}
			}
			
			// if reached here, that means that there is no social network account for that name
			SocialNetworkAccount sn = new SocialNetworkAccount();
			sn.setSocialNetwork(name);
			sn.setLink(link);
			
			sn = saveEntity(sn);
			
			userSocialNetworks.addSocialNetworkAccount(sn);
			userSocialNetworks = saveEntity(userSocialNetworks);
		}
		return userSocialNetworks;
	}
	
	@Override
	@Transactional (readOnly = false)
	public SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link) {
		SocialNetworkAccount account = new SocialNetworkAccount();
		account.setSocialNetwork(name);
		account.setLink(link);
		return saveEntity(account);
	}
	
}
