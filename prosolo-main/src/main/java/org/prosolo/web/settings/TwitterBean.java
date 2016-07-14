package org.prosolo.web.settings;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.user.oauth.OauthAccessToken;
import org.prosolo.common.domainmodel.user.socialNetworks.ServiceType;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.data.SocialNetworkAccountData;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import twitter4j.TwitterException;

/**
 * @author Zoran Jeremic 2013-08-04
 * 
 */

@ManagedBean(name = "twitterBean")
@Component("twitterBean")
@Scope("view")
public class TwitterBean implements Serializable {

	private static final long serialVersionUID = -4161095810221302021L;

	private static Logger logger = Logger.getLogger(TwitterBean.class);

	private String accountStatusMessage;
	private String screenName;
	private String twitterProfile;

	@Inject
	private TwitterApiManager twitterApiManager;
	@Inject
	private UserOauthTokensManager userOauthTokensManager;
	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private ProfileSettingsBean profileSettingsBean;
	@Inject
	private AnalyticalServiceCollector analyticalServiceCollector;
	@Inject
	private SocialNetworksManager socialNetworksManager;

	private OauthAccessToken accessToken = null;
	private boolean connectedToTwitter;

	private String hashTags;

	@PostConstruct
	public void init() {
		// checking whether there is "oauth_verifier" query parameter in URL. If there are, that means that 
		// user is doing Twitter authentication
		try {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			Map<String, String> parameterMap = (Map<String, String>) externalContext.getRequestParameterMap();
			String oauthVerifier = parameterMap.get("oauth_verifier");
			
			if (oauthVerifier != null && !oauthVerifier.isEmpty()) {
				accessToken = twitterApiManager.verifyAndGetTwitterAccessToken(loggedUser.getUserId(), oauthVerifier);
				
				if (accessToken != null) {
					PageUtil.fireSuccessfulInfoMessage("You have connected your Twitter account with ProSolo");
					
					SocialNetworkAccountData twitterData = profileSettingsBean.getSocialNetworksData().getSocialNetworkAccountDatas().get(SocialNetworkName.TWITTER.toString());
					
					if (twitterData != null) {
						twitterData.setLinkEdit(accessToken.getProfileLink());
//						profileSettingsBean.saveSocialNetworkChanges();
						
						if (twitterData.isChanged()) {
							if (twitterData.getId() == 0) {
								socialNetworksManager.createSocialNetworkAccount(
										twitterData.getSocialNetworkName(),
										twitterData.getLinkEdit());
							} else {
								socialNetworksManager.updateSocialNetworkAccount(twitterData, loggedUser.getUserId());
							}
						}
						Ajax.update("settings:socialNetworksSettingsForm:twitterProfileUrl");
						
						analyticalServiceCollector.updateTwitterUser(loggedUser.getUserId(), true);
					}
				}
			} else {
				accessToken = twitterApiManager.getOauthAccessToken(loggedUser.getUserId());
			}
		} catch (Exception e) {
			logger.error("Exception in checking twitter status for user:" + loggedUser.getSessionData().getName() + " "
					+ loggedUser.getSessionData().getLastName(), e);
		}

		this.connectedToTwitter = accessToken != null;
	}

	public void initHashTags() {
//		// User user = hashtagManager.merge(loggedUser.getUser());
//		// TopicPreference topicPreference =
//		// user.getPreferences(TopicPreference.class);
//		// Set<Annotation> preferredHashTags =
//		// topicPreference.getPreferredHashTags();
//		this.hashTags = AnnotationUtil
//				.getAnnotationsAsSortedCSV(tagManager.getSubscribedHashtags(loggedUser.getUser()));
	}

//	public void initAccountStatusMessage() {
//		if (!allOk) {
//			this.setScreenName(null);
//		}
//	}

	public void startTweeterOauthAuthProcess() throws TwitterException, IOException {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String callbackUrl = getCallbackUrl(request);
		
		String url = twitterApiManager.getTwitterTokenUrl(loggedUser.getUserId(), callbackUrl);
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		externalContext.redirect(url);
	}

	public String getCallbackUrl(HttpServletRequest request) {
		String host = request.getServerName();
		int portNumber = request.getServerPort();
		String port = null;
		port = (portNumber != 80) ? (":" + portNumber) : "";
		String app = request.getContextPath();
		String publicLink = "http://" + host + port + app + "/settings";
		return publicLink;
	}

	public void disconnectUserAccount() {
		logger.debug("Disconnecct from twitter for user " + loggedUser.getUserId());

		long deletedUserId = userOauthTokensManager.deleteUserOauthAccessToken(loggedUser.getUserId(), ServiceType.TWITTER);
		analyticalServiceCollector.updateTwitterUser(deletedUserId, false);
		
		this.connectedToTwitter = false;

		PageUtil.fireSuccessfulInfoMessage("socialNetworksSettingsForm:socialNetworksFormGrowl",
				"Your Twitter account is disconnected from ProSolo.");
	}

//	public void updateHashTagsAction() {
//		String context = PageUtil.getPostParameter("context");
//		loggedUser.refreshUser();
//		Set<Tag> hashTagList = tagManager.getOrCreateTags(AnnotationUtil.getTrimmedSplitStrings(hashTags));
//		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(loggedUser.getUser(),
//				TopicPreference.class);
//		List<Tag> oldHashTags = new ArrayList<Tag>();
//		oldHashTags.addAll(topicPreference.getPreferredHashtags());
//		topicPreference.setPreferredHashtags(new HashSet<Tag>(hashTagList));
//		tagManager.saveEntity(topicPreference);
//		System.out.println("UPDATING HASHTAGS FOR USER 1");
//		// twitterStreamsManager
//		// .updateHashTagsForUserAndRestartStream(oldHashTags,
//		// topicPreference.getPreferredHashtags(),
//		// loggedUser.getUser().getId());
//		eventFactory.generateUpdateHashtagsEvent(loggedUser.getUser(), oldHashTags,
//				topicPreference.getPreferredHashtags(), null, loggedUser.getUser(), context);
//		PageUtil.fireSuccessfulInfoMessage("Updated twitter hashtags!");
//	}

//	public Collection<Tag> unfollowHashtags(List<String> hashtags) {
//		User user = tagManager.merge(loggedUser.getUser());
//		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user, TopicPreference.class);
//
//		Set<Tag> removedHashtags = new HashSet<Tag>();
//
//		Iterator<Tag> preferedHashTags = topicPreference.getPreferredHashtags().iterator();
//
//		for (String tag : hashtags) {
//			while (preferedHashTags.hasNext()) {
//				Tag hashtag = preferedHashTags.next();
//
//				if (hashtag.getTitle().equals(tag)) {
//					removedHashtags.add(hashtag);
//					preferedHashTags.remove();
//				}
//			}
//		}
//		tagManager.saveEntity(topicPreference);
//
//		Set<TargetLearningGoal> tGoals = user.getLearningGoals();
//
//		for (TargetLearningGoal tGoal : tGoals) {
//			LearningGoal lGoal = tGoal.getLearningGoal();
//			Iterator<Tag> lGoalHashTags = lGoal.getHashtags().iterator();
//
//			for (String tag : hashtags) {
//				while (lGoalHashTags.hasNext()) {
//
//					Tag hashtag = lGoalHashTags.next();
//
//					if (hashtag.getTitle().equals(tag)) {
//						removedHashtags.add(hashtag);
//						lGoalHashTags.remove();
//					}
//				}
//			}
//			tagManager.saveEntity(lGoal);
//		}
//
//		return removedHashtags;
//		return null;
//	}

	/*
	 * GETTERS / SETTERS
	 */
	public String getAccountStatusMessage() {
		return accountStatusMessage;
	}

	public void setAccountStatusMessage(String accountStatusMessage) {
		this.accountStatusMessage = accountStatusMessage;
	}

	public String getHashTags() {
		return hashTags;
	}

	public void setHashTags(String hashtags) {
		this.hashTags = hashtags;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getTwitterProfile() {
		return twitterProfile;
	}

	public void setTwitterProfile(String twitterProfile) {
		this.twitterProfile = twitterProfile;
	}

	public boolean isConnectedToTwitter() {
		return connectedToTwitter;
	}
	
	public String getTwitterUsername() {
		return accessToken != null ? accessToken.getProfileLink() : "";
	}

}
