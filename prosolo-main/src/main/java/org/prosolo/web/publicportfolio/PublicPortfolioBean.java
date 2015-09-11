package org.prosolo.web.publicportfolio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.user.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.SocialNetworkName;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.displayers.PortfolioSocialActivitiesDisplayer;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.prosolo.web.portfolio.data.GoalStatisticsData;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.prosolo.web.portfolio.util.AchievedCompetenceDataConverter;
import org.prosolo.web.portfolio.util.CompletedGoalDataConverter;
import org.prosolo.web.portfolio.util.ExternalCreditsDataConverter;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="publicportfolio")
@Component("publicportfolio")
@Scope("view")
public class PublicPortfolioBean implements Serializable {
	
	private static final long serialVersionUID = 5004810142702166055L;

	private static Logger logger = Logger.getLogger(PublicPortfolioBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private UserManager userManager;
	@Autowired private ExternalCreditsDataConverter externalCreditsDataConverter;
	@Autowired private CompletedGoalDataConverter completedGoalDataConverter;
	@Autowired private AchievedCompetenceDataConverter achievedCompetenceDataConverter;
	@Autowired private SocialNetworksManager socialNetworksManager;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;
	
	private List<GoalData> ongoingGoals;

	private List<GoalData> completedGoals;
	
	private List<AchievedCompetenceData> ongoingCompetences;
	private List<AchievedCompetenceData> achievedComps;
	private GoalStatisticsData goalStats;
	
	private List<ExternalCreditData> externalCredits;
	
	private User profileOwner;
	private UserData profileOwnerData;
	
	private SocialNetworksData socialNetworksData;
	
	private PortfolioSocialActivitiesDisplayer portfolioActivitiesDisplayer;
	
	public void init(){
		logger.debug("initializing");
//		String accessedFromIpAddress = accessResolver.findRemoteIPAddress();

		initializeUser();
		portfolioActivitiesDisplayer = ServiceLocator.getInstance().getService(PortfolioSocialActivitiesDisplayer.class);
		portfolioActivitiesDisplayer.init(loggedUser.getUser(), loggedUser.getLocale(), null, userId);
	}
	
	private void initializeUser() {
		if (userId > 0) {
			try {
				profileOwner = userManager.loadResource(User.class, userId, true);
				profileOwnerData = new UserData(profileOwner);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				PageUtil.redirectToLoginPage();
			}
		} else if (loggedUser != null && loggedUser.isLoggedIn()) {
			profileOwner = loggedUser.refreshUser();
			profileOwnerData = new UserData(profileOwner);
		} else {
			// user is not authenticated and is accessing a page of a user that does not exist
			PageUtil.redirectToLoginPage();
		}
	}

	public void initCompletedGoals() {
		if (profileOwner != null) {
			profileOwner = portfolioManager.merge(profileOwner);
			
			// ongoing goals
			ongoingGoals = new ArrayList<GoalData>();
			
			Collection<TargetLearningGoal> ongoingTargetGoals = portfolioManager.getPublicOngoingTargetLearningGoals(profileOwner);
			
			if (ongoingTargetGoals != null && !ongoingTargetGoals.isEmpty()) {
				for (TargetLearningGoal targetLearningGoal : ongoingTargetGoals) {
					GoalData goalData = new GoalData(targetLearningGoal);
					
					goalData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(
							TargetLearningGoal.class, 
							targetLearningGoal.getId(), 
							(Session) portfolioManager.getPersistence().currentManager()));
					
					goalData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(
							TargetLearningGoal.class, 
							targetLearningGoal.getId(), 
							(Session) portfolioManager.getPersistence().currentManager()));
					
					goalData.setBadgeCount(badgeManager.getBadgeCountForResource(
							TargetLearningGoal.class, 
							targetLearningGoal.getId(), 
							(Session) portfolioManager.getPersistence().currentManager()));
					
					ongoingGoals.add(goalData);
				}
			}
			
			
			// completed goals
			completedGoals = new ArrayList<GoalData>();
			
			// archieved completed goals
			Collection<CompletedGoal> archievedCompletedGoals = portfolioManager.getPublicCompletedArchivedGoals(profileOwner);
	
			if (archievedCompletedGoals != null && !archievedCompletedGoals.isEmpty()) {
				List<CompletedGoal> completedList = new ArrayList<CompletedGoal>(archievedCompletedGoals);
				this.completedGoals.addAll(completedGoalDataConverter.convertCompletedGoals(completedList));
			}
			
			// nonarchieved completed goals
			Collection<TargetLearningGoal> activeCompletedGoals = portfolioManager.getPublicCompletedNonarchivedLearningGoals(profileOwner);
			
			if (activeCompletedGoals != null && !activeCompletedGoals.isEmpty()) {
				List<TargetLearningGoal> completedList = new ArrayList<TargetLearningGoal>(activeCompletedGoals);
				this.completedGoals.addAll(completedGoalDataConverter.convertTargetGoals(completedList));
			}
			Collections.sort(completedGoals);
			initGoalStatistics();
		}
	}
	
	public void initGoalStatistics() {
		goalStats = new GoalStatisticsData();
		
		int diffSeconds = 0;
		
		if (completedGoals != null && !completedGoals.isEmpty()) {
			int totalDiffs = 0;
			
			for (GoalData cGoalData : completedGoals) {
				if (cGoalData.getDateStarted() != null && cGoalData.getDateCompleted() != null) {
					Date timeCompleted = cGoalData.getDateCompleted();
					Date timeStarted = cGoalData.getDateStarted();
					
					if (timeCompleted != null && timeStarted != null)
						totalDiffs += timeCompleted.getTime() - timeStarted.getTime();
				}
			}
			
			diffSeconds = totalDiffs / completedGoals.size();
		}
		
		goalStats.setAverageTime(DateUtil.getTimeDuration((long) diffSeconds));
		
		int completedGoalsNo = completedGoals.size();
		goalStats.setCompletedNo(completedGoalsNo);
		goalStats.setTotalNo(completedGoalsNo + portfolioManager.merge(profileOwner).getLearningGoals().size());
	}

	public void initAchievedCompetences() {
		if (profileOwner != null) {
			this.achievedComps = new ArrayList<AchievedCompetenceData>();
			
			List<AchievedCompetence> achievedCompList = portfolioManager.getPublicAchievedCompetences(profileOwner);
			this.achievedComps = achievedCompetenceDataConverter.convertAchievedComps(achievedCompList);
			
			List<TargetCompetence> nonarchievedCompList = portfolioManager.getPublicCompletedNonarchivedTargetCompetences(profileOwner);
			this.achievedComps.addAll(achievedCompetenceDataConverter.convertTargetCompetences(nonarchievedCompList));
			
			Collections.sort(this.achievedComps);
			
			// ongoing competences
			
			this.ongoingCompetences = new ArrayList<AchievedCompetenceData>();
			List<TargetCompetence> ongoingCompList = portfolioManager.getPublicOngoingTargetCompetences(profileOwner);
			this.ongoingCompetences.addAll(achievedCompetenceDataConverter.convertTargetCompetences(ongoingCompList));
			
		}
	}
	
	public void initExternalCredits() {
		if (profileOwner != null) {
			if (externalCredits == null) {
				logger.debug("Initializing external credits for user "+profileOwner);
				
				User user = loggedUser != null ? loggedUser.getUser() : null;
				Locale locale = loggedUser != null ? loggedUser.getLocale() : new Locale("en", "US");;
				
				this.externalCredits = externalCreditsDataConverter.convertExternalCredits(
						portfolioManager.getVisibleExternalCredits(profileOwner),
						user,
						locale);
			}
		}
	}
	
	public void initSocialNetworks() {
		if (socialNetworksData == null) {
			logger.debug("Initializing social networks data for user "+profileOwner);
			
			UserSocialNetworks socialNetworks = socialNetworksManager.getSocialNetworks(profileOwner);
			
			socialNetworksData = new SocialNetworksData();
			socialNetworksData.setId(socialNetworks.getId());
			
			SocialNetworkAccount twitterAccount = socialNetworks.getAccount(SocialNetworkName.TWITTER);
			
			if (twitterAccount != null) {
				socialNetworksData.setTwitterLink(twitterAccount.getLink());
				socialNetworksData.setTwitterLinkEdit(twitterAccount.getLink());
			}
			
			SocialNetworkAccount facebookAccount = socialNetworks.getAccount(SocialNetworkName.FACEBOOK);
			
			if (facebookAccount != null) {
				socialNetworksData.setFacebookLink(facebookAccount.getLink());
				socialNetworksData.setFacebookLinkEdit(facebookAccount.getLink());
			}
			
			SocialNetworkAccount gplusAccount = socialNetworks.getAccount(SocialNetworkName.GPLUS);
			
			if (gplusAccount != null) {
				socialNetworksData.setGplusLink(gplusAccount.getLink());
				socialNetworksData.setGplusLinkEdit(gplusAccount.getLink());
			}
			
			SocialNetworkAccount blogAccount = socialNetworks.getAccount(SocialNetworkName.BLOG);
			
			if (blogAccount != null) {
				socialNetworksData.setBlogLink(blogAccount.getLink());
				socialNetworksData.setBlogLinkEdit(blogAccount.getLink());
			}
		}
	}
	
	// Status Wall
	public void initializeActivities() {
		logger.debug("Initializing public portfolio activity wall");
		
		portfolioActivitiesDisplayer.initializeActivities();
		logger.debug("Initialized public portfolio activity wall");
	}
	
	public void refresh() {
		
	}
	
	public void loadMoreActivities() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", "publicprofile."+userId);
		parameters.put("link", "loadMore");
		
		portfolioActivitiesDisplayer.loadMoreActivities(parameters);
	}
	
	public List<SocialActivityData> getAllActivities() {
		return portfolioActivitiesDisplayer != null ? portfolioActivitiesDisplayer.getAllActivities() : new ArrayList<SocialActivityData>();
	}
	
	public boolean isMoreToLoad() {
		return portfolioActivitiesDisplayer.isMoreToLoad();
	}
	
	/*
	 * PARAMETERS
	 */
	private long userId = -1;
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public long getUserId() {
		return userId;
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public List<GoalData> getCompletedGoals() {
		return completedGoals;
	}

	public List<GoalData> getOngoingGoals() {
		return ongoingGoals;
	}

	public List<AchievedCompetenceData> getAchievedComps() {
		return achievedComps;
	}

	public GoalStatisticsData getGoalStats() {
		return goalStats;
	}

	public User getProfileOwner() {
		return profileOwner;
	}
	
	public UserData getProfileOwnerData() {
		return profileOwnerData;
	}

	public List<ExternalCreditData> getExternalCredits() {
		return externalCredits;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public List<AchievedCompetenceData> getOngoingCompetences() {
		return ongoingCompetences;
	}
	
}
