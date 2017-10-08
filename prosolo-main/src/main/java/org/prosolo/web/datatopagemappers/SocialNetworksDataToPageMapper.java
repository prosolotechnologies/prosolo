package org.prosolo.web.datatopagemappers;

import java.util.Set;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.SocialNetworksData;
import org.prosolo.web.profile.data.UserSocialNetworksData;

public class SocialNetworksDataToPageMapper implements IDataToPageMapper<SocialNetworksData, UserSocialNetworks> {

	/**
	 * Maps supplied data to object that is shown on page
	 */
	@Override
	public SocialNetworksData mapDataToPageObject(UserSocialNetworks userSocialNetworks) {

		Set<SocialNetworkAccount> socialNetworkAccounts = userSocialNetworks.getSocialNetworkAccounts();

		SocialNetworksData socialNetworksData = new SocialNetworksData();
		socialNetworksData.setId(userSocialNetworks.getId());

		for (SocialNetworkAccount account : socialNetworkAccounts) {
			socialNetworksData.setAccount(account);
		}
		return socialNetworksData;
	}


	public SocialNetworksData mapDataToPageObjectData(UserSocialNetworksData userSocialNetworks) {

		Set<SocialNetworkAccountData> socialNetworkAccounts = userSocialNetworks.getSocialNetworkAccounts();

		SocialNetworksData socialNetworksData = new SocialNetworksData();
		socialNetworksData.setId(userSocialNetworks.getId());

		for (SocialNetworkAccountData account : socialNetworkAccounts) {
			socialNetworksData.setAccount(account);
		}
		return socialNetworksData;
	}

}
