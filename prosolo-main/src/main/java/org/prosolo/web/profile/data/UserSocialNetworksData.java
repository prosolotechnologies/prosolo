package org.prosolo.web.profile.data;

import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;

import java.util.Set;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-07
 * @since 1.0.0
 */
public class UserSocialNetworksData {


    private long id;
    private long userId;
    private Set<SocialNetworkAccountData> socialNetworkAccounts;

    public UserSocialNetworksData(){}

    public UserSocialNetworksData(UserSocialNetworks userSocialNetworks, Set<SocialNetworkAccountData> socialNetworkAccountData){
        this();
        this.id = userSocialNetworks.getId();
        this.userId = userSocialNetworks.getUser().getId();
        this.socialNetworkAccounts = socialNetworkAccountData;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Set<SocialNetworkAccountData> getSocialNetworkAccounts() {
        return socialNetworkAccounts;
    }

    public void setSocialNetworkAccounts(Set<SocialNetworkAccountData> socialNetworkAccounts) {
        this.socialNetworkAccounts = socialNetworkAccounts;
    }


}
