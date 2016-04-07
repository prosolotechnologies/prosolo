package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.general.AbstractManager;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id);
	
	UserSocialNetworks getSocialNetworks(User user);
	
	UserSocialNetworks updateSocialNetwork(UserSocialNetworks userSocialNetworks, SocialNetworkName name, String link);

	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link);
	
}
