package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-09
 * @since 1.0.0
 */

@Component
public class UserSocialNetworksDataFactory {

    @Inject
    private UserManager userManager;
    @Inject
    private SocialNetworkAccountDataFactory socialNetworkAccountDataFactory;

    public UserSocialNetworks getUserSocialNetworks(UserSocialNetworksData userSocialNetworksData){
        UserSocialNetworks userSocialNetworks = new UserSocialNetworks();
        userSocialNetworks.setId(userSocialNetworksData.getId());
        userSocialNetworks.setUser(userManager.getUserById(userSocialNetworksData.getUserId()));

        Set<SocialNetworkAccount> accountsSet = new HashSet<>();
        for(SocialNetworkAccountData s : userSocialNetworksData.getSocialNetworkAccounts()){
            SocialNetworkAccount socialNetworkAccount = socialNetworkAccountDataFactory.getSocialNetworkAccount(s);
            accountsSet.add(socialNetworkAccount);
        }

        userSocialNetworks.setSocialNetworkAccounts(accountsSet);

        return userSocialNetworks;
    }
}
