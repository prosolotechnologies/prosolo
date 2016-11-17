package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id) throws ResourceCouldNotBeLoadedException;
	
	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link);
	
	void addSocialNetworkAccount(long userId, SocialNetworkName name, String link) throws ResourceCouldNotBeLoadedException;

	void updateSocialNetworkAccount(long id, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	void updateSocialNetworkAccount(SocialNetworkAccount account, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	SocialNetworkAccount getSocialNetworkAccount(long userId, SocialNetworkName socialNetworkName);
	
}
