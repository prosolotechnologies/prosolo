package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.prosolo.web.profile.data.SocialNetworksData;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    public UserSocialNetworks getUserSocialNetworks(SocialNetworksData socialNetworksData){
        UserSocialNetworks userSocialNetworks = new UserSocialNetworks();
        userSocialNetworks.setId(socialNetworksData.getId());
        try {
            userSocialNetworks.setUser(userManager.loadResource(User.class, socialNetworksData.getUserId()));
        } catch (ResourceCouldNotBeLoadedException e) {
            e.printStackTrace();
        }

        Map<String,SocialNetworkAccount> accountDataMap = new HashMap<>();

        for(SocialNetworkAccountData s : socialNetworksData.getSocialNetworkAccountsData().values()){
            if(s.getId() != 0) {
                SocialNetworkAccount socialNetworkAccount = socialNetworkAccountDataFactory.getSocialNetworkAccount(s);
                accountDataMap.put(socialNetworkAccount.getSocialNetwork().toString(), socialNetworkAccount);
            }
        }
        Set<SocialNetworkAccount> socialNetworkAccounts = new HashSet<>(accountDataMap.values());

        userSocialNetworks.setSocialNetworkAccounts(socialNetworkAccounts);

        return userSocialNetworks;
    }
}
