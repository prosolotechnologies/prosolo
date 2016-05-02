package org.prosolo.web.datatopagemappers;

import java.util.Set;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.data.SocialNetworksData;

public class SocialNetworksDataToPageMapper implements IDataToPageMapper<SocialNetworksData> {

	private SocialNetworksManager socialNetworksManager;
	private LoggedUserBean loggedUser;

	public SocialNetworksDataToPageMapper(SocialNetworksManager socialNetworksManager, LoggedUserBean loggedUser) {
		this.socialNetworksManager = socialNetworksManager;
		this.loggedUser = loggedUser;
	}

	/**
	 * Maps supplied data to object that is shown on page
	 */
	@Override
	public SocialNetworksData mapDataToPageObject(SocialNetworksData socialNetworksData) {

		UserSocialNetworks userSocialNetworks = socialNetworksManager.getSocialNetworks(loggedUser.getUser());
		Set<SocialNetworkAccount> socialNetworkAccounts = userSocialNetworks.getSocialNetworkAccounts();

		socialNetworksData = new SocialNetworksData();
		socialNetworksData.setId(userSocialNetworks.getId());

		for (SocialNetworkAccount account : socialNetworkAccounts) {
			socialNetworksData.setAccountLink(account);
		}
		return socialNetworksData;
	}

}
