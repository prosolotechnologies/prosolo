/**
 * 
 */
package org.prosolo.web.portfolio.data;

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

	private static final String FACEBOOK = "https://www.facebook.com/"; 
	private static final String TWITTER = "https://twitter.com/"; 
	private static final String LINKEDIN = "https://www.linkedin.com/in/"; 
	
	private long id;

	private Map<String, SocialNetworkAccountData> socialNetworkAccountDatas;

	public SocialNetworksData() {
		socialNetworkAccountDatas = new LinkedHashMap<>();
		socialNetworkAccountDatas.put(SocialNetworkName.LINKEDIN.toString(),
				new SocialNetworkAccountData(SocialNetworkName.LINKEDIN));
		socialNetworkAccountDatas.put(SocialNetworkName.TWITTER.toString(),
				new SocialNetworkAccountData(SocialNetworkName.TWITTER));
		socialNetworkAccountDatas.put(SocialNetworkName.FACEBOOK.toString(),
				new SocialNetworkAccountData(SocialNetworkName.FACEBOOK));
		socialNetworkAccountDatas.put(SocialNetworkName.BLOG.toString(),
				new SocialNetworkAccountData(SocialNetworkName.BLOG));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, SocialNetworkAccountData> getSocialNetworkAccountDatas() {
		return socialNetworkAccountDatas;
	}

	public void setSocialNetworkAccountDatas(Map<String, SocialNetworkAccountData> socialNetworkAccountDatas) {
		this.socialNetworkAccountDatas = socialNetworkAccountDatas;
	}

	public void setAccount(SocialNetworkAccount account) {
		SocialNetworkAccountData socialNetworkAccountData = socialNetworkAccountDatas
				.get(account.getSocialNetwork().toString());
		String prefixUrl = getPrefixUrlForSocialNetwork(account.getSocialNetwork());
		socialNetworkAccountData.setLinkEdit(account.getLink());
		socialNetworkAccountData.setLink(prefixUrl + account.getLink());
		socialNetworkAccountData.setId(account.getId());
	}

	private String getPrefixUrlForSocialNetwork(SocialNetworkName name) {
		switch(name) {
			case BLOG:
				return "";
			case FACEBOOK:
				return FACEBOOK;
			case TWITTER:
				return TWITTER;
			case LINKEDIN:
				return LINKEDIN;
			default:
				return "";
		}
	}

}
