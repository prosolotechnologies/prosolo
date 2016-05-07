package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.web.portfolio.data.SocialNetworkAccountData;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id);
	
	UserSocialNetworks getSocialNetworks(User user);
	
	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link);

	void updateSocialNetworkAccount(SocialNetworkAccountData socialNetowrkAccountData) throws DbConnectionException;
	
}
