package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.portfolio.data.SocialNetworkAccountData;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id) throws ResourceCouldNotBeLoadedException;
	
	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link);

	void updateSocialNetworkAccount(SocialNetworkAccountData socialNetowrkAccountData) throws DbConnectionException;
	
}
