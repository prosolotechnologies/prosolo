package org.prosolo.web.datatopagemappers;

import java.util.Set;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.data.SocialNetworksData;

public class SocialNetworksDataToPageMapper implements IDataToPageMapper<SocialNetworksData> {
	UserSocialNetworks userSocialNetworks;

	public SocialNetworksDataToPageMapper(UserSocialNetworks userSocialNetworks) {
		this.userSocialNetworks = userSocialNetworks;
	}

	/**
	 * Maps supplied data to object that is shown on page
	 */
	@Override
	public SocialNetworksData mapDataToPageObject(SocialNetworksData socialNetworksData) {

		Set<SocialNetworkAccount> socialNetworkAccounts = userSocialNetworks.getSocialNetworkAccounts();

		socialNetworksData = new SocialNetworksData();
		socialNetworksData.setId(userSocialNetworks.getId());

		for (SocialNetworkAccount account : socialNetworkAccounts) {
			socialNetworksData.setAccount(account);
		}
		return socialNetworksData;
	}

}
