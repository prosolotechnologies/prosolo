package org.prosolo.web.settings;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.OauthAccessToken;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.twitter.TwitterApiManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.portfolio.PortfolioBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired private TwitterApiManager twitterApiManager;
	@Autowired private UserManager userManager;
	@Autowired private UserOauthTokensManager userOauthTokensManager;
	@Autowired private LoggedUserBean loggedUser;
	//@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Autowired private TagManager tagManager;
	@Autowired private EventFactory eventFactory;
	@Autowired AnalyticalServiceCollector analyticalServiceCollector;

	private boolean allOk;
	private boolean disconnected;
	
	private String hashTags;
	
	@PostConstruct
    public boolean init(){
		try {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			Map<String, String> parameterMap = (Map<String, String>) externalContext.getRequestParameterMap();
			String oauthVerifier = parameterMap.get("oauth_verifier");
			
			//@SuppressWarnings("unused")
			OauthAccessToken accessToken= twitterApiManager.verifyAndGetTwitterAccessToken(loggedUser.getUser(), oauthVerifier);
			 
			String screenN=accessToken.getProfileName();
			
			String twitterProfileUrl = "https://twitter.com/"+screenN;
			setScreenName(screenN);
			setTwitterProfile(twitterProfileUrl);
			
			// set Twitter link if it is not already set
			PortfolioBean portfolio = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);
			
			if (portfolio != null) {
				String twitterLink = portfolio.getSocialNetworksData().getTwitterLink();
				
				if (twitterLink == null || !twitterProfileUrl.equals(twitterLink)) {
					portfolio.getSocialNetworksData().setTwitterLinkEdit(twitterProfileUrl);
					portfolio.saveSocialNetworks();
					PageUtil.fireSuccessfulInfoMessage("socialNetworksSettingsForm:socialNetworksFormGrowl", "Social networks updated!");
				}
			}
	 
			allOk = true;
		} catch (java.lang.IllegalStateException e) {
			//logger.error("IllegalStateException in checking twitter status for user:"+loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname()+e.getLocalizedMessage());
			allOk = false;
		} catch (Exception e) {
			logger.error("Exception in checking twitter status for user:" +loggedUser.getUser().getName()+" "+loggedUser.getUser().getLastname(),e);
			allOk = false;
		}
		this.initAccountStatusMessage();
		this.initHashTags();
		return allOk;
	}
	
	public void initHashTags(){
//		User user = hashtagManager.merge(loggedUser.getUser());
//		TopicPreference topicPreference = user.getPreferences(TopicPreference.class);
//		Set<Annotation> preferredHashTags = topicPreference.getPreferredHashTags();
		this.hashTags = AnnotationUtil.getAnnotationsAsSortedCSV(tagManager.getSubscribedHashtags(loggedUser.getUser()));
	}
	
	public void initAccountStatusMessage() {
		if (!allOk) {
			this.setScreenName(null);
		}
	}

	public void startTweeterOauthAuthProcess() throws TwitterException,	IOException {
		// check if we have the credentials id not redirect;
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String callbackUrl = getCallbackUrl(request);
		OauthAccessToken oauthAccessToken = twitterApiManager.getOauthAccessToken(loggedUser.getUser());
		
		if (oauthAccessToken == null) {
			String url = twitterApiManager.getTwitterTokenUrl(loggedUser.getUser(), callbackUrl);
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(url);
		} 
	}
	
	public String getCallbackUrl(HttpServletRequest request) {
		String host = request.getServerName();
		int portNumber = request.getServerPort();
		String port = null;
		port = (portNumber != 80) ? (":" + portNumber) : "";
		String app = request.getContextPath();
		String servlet = request.getServletPath();
		String publicLink = "http://" + host + port + app + servlet	+ "?tab=socialNetworks";
		return publicLink;
	}
	
	public void disconnectUserAccount(){
		logger.debug("disconnectUserAccount for:"+loggedUser.getUser().getId());
		
		PortfolioBean portfolio = PageUtil.getSessionScopedBean("portfolio", PortfolioBean.class);
		
		if (portfolio != null) {
			String twitterLink = portfolio.getSocialNetworksData().getTwitterLink();
			
			if (twitterLink != null && twitterLink.length() > 0) {
				portfolio.getSocialNetworksData().setTwitterLinkEdit("");
				portfolio.saveSocialNetworks();
			}
		}
		
		long deletedUserId=userOauthTokensManager.deleteUserOauthAccessToken(loggedUser.getUser(), ServiceType.TWITTER);
		 analyticalServiceCollector.updateTwitterUser(deletedUserId,false);
		this.disconnected = true;
		this.setScreenName(null);
 		PageUtil.fireSuccessfulInfoMessage("socialNetworksSettingsForm:socialNetworksFormGrowl", "Your ProSolo account is now disconnect from your Twitter account.");
 		allOk = false;
	}
	
	public void updateHashTagsAction() {
		loggedUser.refreshUser();
		Set<Tag> hashTagList = tagManager.getOrCreateTags(AnnotationUtil.getTrimmedSplitStrings(hashTags));
		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(loggedUser.getUser(), TopicPreference.class);
		List<Tag> oldHashTags = new ArrayList<Tag>();
		oldHashTags.addAll(topicPreference.getPreferredHashtags());
		topicPreference.setPreferredHashtags(new HashSet<Tag>(hashTagList));
		tagManager.saveEntity(topicPreference);
		System.out.println("UPDATING HASHTAGS FOR USER 1");
		//twitterStreamsManager
				//.updateHashTagsForUserAndRestartStream(oldHashTags, topicPreference.getPreferredHashtags(), loggedUser.getUser().getId());
		eventFactory.generateUpdateHashtagsEvent(loggedUser.getUser(),oldHashTags, topicPreference.getPreferredHashtags(),null,loggedUser.getUser());
		PageUtil.fireSuccessfulInfoMessage("Updated twitter hashtags!");
	}

	public Collection<Tag> unfollowHashtags(List<String> hashtags) {
		User user = tagManager.merge(loggedUser.getUser());
		TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user,TopicPreference.class);
		
		Set<Tag> removedHashtags = new HashSet<Tag>();
		
		Iterator<Tag> preferedHashTags = topicPreference.getPreferredHashtags().iterator();
		
		for (String tag : hashtags) {
			while (preferedHashTags.hasNext()) {
				Tag hashtag = preferedHashTags.next();
				
				if (hashtag.getTitle().equals(tag)) {
					removedHashtags.add(hashtag);
					preferedHashTags.remove();
				}
			}
		}
		tagManager.saveEntity(topicPreference);
		
		Set<TargetLearningGoal> tGoals=user.getLearningGoals();
		
		for (TargetLearningGoal tGoal : tGoals) {
			LearningGoal lGoal = tGoal.getLearningGoal();
			Iterator<Tag> lGoalHashTags = lGoal.getHashtags().iterator();
			
			for (String tag : hashtags) {
				while (lGoalHashTags.hasNext()) {
					
					Tag hashtag = lGoalHashTags.next();
					
					if (hashtag.getTitle().equals(tag)) {
						removedHashtags.add(hashtag);
						lGoalHashTags.remove();
					}
				}
			}
			tagManager.saveEntity(lGoal);
		}
		
		return removedHashtags;
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getAccountStatusMessage() {
		return accountStatusMessage;
	}

	public void setAccountStatusMessage(String accountStatusMessage) {
		this.accountStatusMessage = accountStatusMessage;
	}
	
	public boolean isOK() {
		return allOk;
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

	public boolean isAllOk() {
		return allOk;
	}

	public boolean isDisconnected() {
		return disconnected;
	}
	
}
