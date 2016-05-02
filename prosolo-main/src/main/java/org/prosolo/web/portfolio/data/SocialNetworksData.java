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

	private long id;

	private Map<String, SocialNetworkAccount> socialNetworkAccounts;

	public SocialNetworksData() {
		socialNetworkAccounts = new LinkedHashMap<>();
		socialNetworkAccounts.put(SocialNetworkName.LINKEDIN.toString(),
				new SocialNetworkAccount(SocialNetworkName.LINKEDIN));
		socialNetworkAccounts.put(SocialNetworkName.TWITTER.toString(),
				new SocialNetworkAccount(SocialNetworkName.TWITTER));
		socialNetworkAccounts.put(SocialNetworkName.FACEBOOK.toString(),
				new SocialNetworkAccount(SocialNetworkName.FACEBOOK));
		socialNetworkAccounts.put(SocialNetworkName.BLOG.toString(), new SocialNetworkAccount(SocialNetworkName.BLOG));
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, SocialNetworkAccount> getSocialNetworkAccounts() {
		return socialNetworkAccounts;
	}

	public void setSocialNetworkAccounts(Map<String, SocialNetworkAccount> socialNetworkAccounts) {
		this.socialNetworkAccounts = socialNetworkAccounts;
	}

	public void setAccountLink(SocialNetworkAccount account) {
		SocialNetworkAccount socialNetworkAccount = socialNetworkAccounts.get(account.getSocialNetwork().toString());
		socialNetworkAccount.setLink(account.getLink());
		socialNetworkAccount.setLinkEdit(account.getLink());
	}

}
