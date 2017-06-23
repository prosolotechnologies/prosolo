/**
 * 
 */
package org.prosolo.web.profile.data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.web.data.IData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class SocialNetworksData implements Serializable, IData {

	private static final long serialVersionUID = 2744838596870425737L;

	private static final String FACEBOOK_URL_PREFIX = "https://www.facebook.com/"; 
	private static final String TWITTER_URL_PREFIX = "https://twitter.com/"; 
	private static final String LINKEDIN_URL_PREFIX = "https://www.linkedin.com/in/"; 
	
	private long id;

	private Map<String, SocialNetworkAccountData> socialNetworkAccountsData;

	public SocialNetworksData() {
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
		//String prefixUrl = getPrefixUrlForSocialNetwork(account.getSocialNetwork());
		socialNetworkAccountData.setLink(account.getLink());
		socialNetworkAccountData.setLinkEdit(account.getLink());
		socialNetworkAccountData.setId(account.getId());
	}

	private String getPrefixUrlForSocialNetwork(SocialNetworkName name) {
		switch(name) {
			case BLOG:
				return "";
			case FACEBOOK:
				return FACEBOOK_URL_PREFIX;
			case TWITTER:
				return TWITTER_URL_PREFIX;
			case LINKEDIN:
				return LINKEDIN_URL_PREFIX;
			default:
				return "";
		}
	}

}
