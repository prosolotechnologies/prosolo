package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.web.profile.data.SocialNetworkAccountData;
import org.springframework.stereotype.Component;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-09
 * @since 1.2.0
 */

@Component
public class SocialNetworkAccountDataFactory {

    public SocialNetworkAccount getSocialNetworkAccount(SocialNetworkAccountData socialNetworkAccountData){
        SocialNetworkAccount socialNetworkAccount = new SocialNetworkAccount();
        socialNetworkAccount.setId(socialNetworkAccountData.getId());
        socialNetworkAccount.setLink(socialNetworkAccountData.getLink());
        socialNetworkAccount.setSocialNetwork(socialNetworkAccountData.getSocialNetworkName());
        return socialNetworkAccount;
    }
}
