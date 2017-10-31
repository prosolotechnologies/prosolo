package org.prosolo.services.nodes;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;
import org.prosolo.services.general.AbstractManager;

public interface SocialNetworksManager extends AbstractManager {

	UserSocialNetworks getSocialNetworks(long id) throws ResourceCouldNotBeLoadedException;
	
	SocialNetworkAccount createSocialNetworkAccount(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException, EventException;

	Result<SocialNetworkAccount> createSocialNetworkAccountAndGetEvents(SocialNetworkName name, String link, UserContextData contextData)
			throws DbConnectionException, EventException;
	
	void addSocialNetworkAccount(long userId, SocialNetworkName name, String link, UserContextData contextData)
			throws ResourceCouldNotBeLoadedException, EventException;

	void updateSocialNetworkAccount(long id, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	void updateSocialNetworkAccount(SocialNetworkAccount account, String newLink) throws DbConnectionException, ResourceCouldNotBeLoadedException;

	SocialNetworkAccount getSocialNetworkAccount(long userId, SocialNetworkName socialNetworkName);

	void saveUserSocialNetworks(UserSocialNetworks userSocialNetworks) throws DbConnectionException;
	
}
