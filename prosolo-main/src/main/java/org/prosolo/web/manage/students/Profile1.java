package org.prosolo.web.manage.students;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.portfolio.data.SocialNetworkAccountData;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "profile1")
@Component
@Scope("view")
public class Profile1 {
	
	@Autowired
	private SocialNetworksManager socialNetworksManager;
	@Autowired
	private LoggedUserBean loggedUserBean;
	
	private UserSocialNetworks userSocialNetworks;
	private SocialNetworksData socialNetworksData;
	private Map<String, String> nameMap = new HashMap<>();

	@PostConstruct
	public void init() {
		userSocialNetworks = socialNetworksManager.getSocialNetworks(loggedUserBean.getUser());
		socialNetworksData = new SocialNetworksDataToPageMapper()
				.mapDataToPageObject(userSocialNetworks);
		nameMap.put(SocialNetworkName.BLOG.toString(), "website");
		nameMap.put(SocialNetworkName.FACEBOOK.toString(), "facebook");
		nameMap.put(SocialNetworkName.GPLUS.toString(), "gplus");
		nameMap.put(SocialNetworkName.LINKEDIN.toString(), "linkedIn");
		nameMap.put(SocialNetworkName.TWITTER.toString(), "twitter");
	}
	
	
	public String getAlternativeName(SocialNetworkName name) {
		return nameMap.get(name.toString());
	}

	public SocialNetworksManager getSocialNetworksManager() {
		return socialNetworksManager;
	}

	public void setSocialNetworksManager(SocialNetworksManager socialNetworksManager) {
		this.socialNetworksManager = socialNetworksManager;
	}

	public LoggedUserBean getLoggedUserBean() {
		return loggedUserBean;
	}

	public void setLoggedUserBean(LoggedUserBean loggedUserBean) {
		this.loggedUserBean = loggedUserBean;
	}

	public UserSocialNetworks getUserSocialNetworks() {
		return userSocialNetworks;
	}

	public void setUserSocialNetworks(UserSocialNetworks userSocialNetworks) {
		this.userSocialNetworks = userSocialNetworks;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public void setSocialNetworksData(SocialNetworksData socialNetworksData) {
		this.socialNetworksData = socialNetworksData;
	}

}
