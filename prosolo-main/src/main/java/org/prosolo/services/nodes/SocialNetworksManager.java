package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.UserSocialNetworksData;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id) throws ResourceCouldNotBeLoadedException;
	
	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException;

	Result<SocialNetworkAccount> createSocialNetworkAccountAndGetEvents(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException;
	
	void addSocialNetworkAccount(long userId, SocialNetworkName name, String link, UserContextData contextData)
			throws ResourceCouldNotBeLoadedException;

	void updateSocialNetworkAccount(long id, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	void updateSocialNetworkAccount(SocialNetworkAccount account, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	SocialNetworkAccount getSocialNetworkAccount(long userId, SocialNetworkName socialNetworkName);

	SocialNetworkAccountData getSocialNetworkAccountData(long userId, SocialNetworkName socialNetworkName);

	UserSocialNetworksData getUserSocialNetworkData(long userId) throws ResourceCouldNotBeLoadedException;

}
