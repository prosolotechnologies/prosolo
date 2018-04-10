/**
 * 
 */
package org.prosolo.web.profile.data;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author "Nikola Milikic"
 * 
 */
public class UserSocialNetworksData implements Serializable {

	private static final long serialVersionUID = 2744838596870425737L;
	
	private long id;
	private long userId;

	private Map<String, SocialNetworkAccountData> socialNetworkAccountsData;

	public UserSocialNetworksData() {
		socialNetworkAccountsData = new LinkedHashMap<>();
		socialNetworkAccountsData.put(SocialNetworkName.LINKEDIN.toString(),
				new SocialNetworkAccountData(SocialNetworkName.LINKEDIN));
		socialNetworkAccountsData.put(SocialNetworkName.TWITTER.toString(),
				new SocialNetworkAccountData(SocialNetworkName.TWITTER));
		socialNetworkAccountsData.put(SocialNetworkName.FACEBOOK.toString(),
				new SocialNetworkAccountData(SocialNetworkName.FACEBOOK));
		socialNetworkAccountsData.put(SocialNetworkName.BLOG.toString(),
				new SocialNetworkAccountData(SocialNetworkName.BLOG));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, SocialNetworkAccountData> getSocialNetworkAccountsData() {
		return socialNetworkAccountsData;
	}

	public void setAccount(SocialNetworkAccount account) {
		SocialNetworkAccountData socialNetworkAccountData = socialNetworkAccountsData
				.get(account.getSocialNetwork().toString());
		socialNetworkAccountData.setLink(account.getLink());
		socialNetworkAccountData.setLinkEdit(account.getLink());
		socialNetworkAccountData.setId(account.getId());
	}

	public void setAccount(SocialNetworkAccountData account) {
		SocialNetworkAccountData socialNetworkAccountData = socialNetworkAccountsData
				.get(account.getSocialNetworkName().toString());
		socialNetworkAccountData.setLink(account.getLink());
		socialNetworkAccountData.setLinkEdit(account.getLink());
		socialNetworkAccountData.setId(account.getId());
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

}
