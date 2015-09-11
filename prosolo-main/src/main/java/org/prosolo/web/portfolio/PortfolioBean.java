package org.prosolo.web.portfolio;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.SocialNetworkName;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.UserSocialNetworks;
import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.displayers.PortfolioSocialActivitiesDisplayer;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.dialogs.data.ExternalCreditData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.portfolio.data.AchievedCompetenceData;
import org.prosolo.web.portfolio.data.CompletedGoalData;
import org.prosolo.web.portfolio.data.GoalStatisticsData;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.prosolo.web.portfolio.util.AchievedCompetenceDataConverter;
import org.prosolo.web.portfolio.util.CompletedGoalDataConverter;
import org.prosolo.web.portfolio.util.ExternalCreditsDataConverter;
import org.prosolo.web.settings.SettingsBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.nodes.ActivityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="portfolio")
@Component("portfolio")
@Scope("session")
public class PortfolioBean implements Serializable {
	
	private static final long serialVersionUID = 5004810142702166055L;

	private static Logger logger = Logger.getLogger(PortfolioBean.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private UserManager userManager;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private ExternalCreditsDataConverter externalCreditsDataConverter;
	@Autowired private CompletedGoalDataConverter completedGoalDataConverter;
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private UploadManager uploadManager;
	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private EventFactory eventFactory;
	@Autowired private AchievedCompetenceDataConverter achievedCompetenceDataConverter;
	@Autowired private SocialNetworksManager socialNetworksManager;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	private List<GoalData> completedGoals;
	private List<GoalData> completedArchivedGoals;
	private List<AchievedCompetenceData> completedComps;
	private List<AchievedCompetenceData> completedAchievedComps;
	private List<ExternalCreditData> externalCredits;
	
	private GoalStatisticsData goalStats;
	private GoalData goalDataToBeSentToGoals;
	
	private ExternalCreditData editedExternalCredit;
	private NewPostData newActivityForExCreditData = new NewPostData();
	private ExternalCreditData exCreditDataToDelete;
	
	private SocialNetworksData socialNetworksData;

	private PortfolioSocialActivitiesDisplayer portfolioActivitiesDisplayer;
	
	@PostConstruct
	public void init() {
		portfolioActivitiesDisplayer = ServiceLocator.getInstance().getService(PortfolioSocialActivitiesDisplayer.class);
		portfolioActivitiesDisplayer.init(loggedUser.getUser(), loggedUser.getLocale(), null, loggedUser.getUser().getId());
	}
	
	@PostConstruct
	public void initCompletedGoals() {
		if (loggedUser.isLoggedIn() && completedGoals == null) {
			this.completedArchivedGoals = new ArrayList<GoalData>();
			
			List<CompletedGoal> completedGoals = portfolioManager.getCompletedGoals(loggedUser.refreshUser());
	
			if (completedGoals != null && !completedGoals.isEmpty()) {
				this.completedArchivedGoals = completedGoalDataConverter.convertCompletedGoals(completedGoals);
			}
			
			populateWithActiveCompletedGoals();
			initGoalStatistics();
		}
	}
	
	public void populateWithActiveCompletedGoals() {
		this.completedGoals = new ArrayList<GoalData>();
		
		if (this.completedArchivedGoals != null) {
			this.completedGoals.addAll(this.completedArchivedGoals);
		}
		
		List<GoalData> completedActiveGoals = learningGoalsBean.getCompletedGoals();
		
		if (completedActiveGoals != null) {
			this.completedGoals.addAll(0, completedActiveGoals);
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
			
			diffSeconds = totalDiffs/completedGoals.size();
		}
		
		goalStats.setAverageTime(DateUtil.getTimeDuration((long) diffSeconds));
		goalStats.setCompletedNo(completedGoals.size());
		goalStats.setTotalNo(learningGoalsBean.getOngoingGoals().size() + completedGoals.size());
	}

	public void initAchievedCompetences() {
		if (completedAchievedComps == null) {
			completedAchievedComps = new ArrayList<AchievedCompetenceData>();
			
			this.completedAchievedComps = achievedCompetenceDataConverter.convertAchievedComps(
					portfolioManager.getAchievedCompetences(loggedUser.getUser()));
			
			populateWithActiveCompletedCompetences();
		}
	}
	
	public void populateWithActiveCompletedCompetences() {
		this.completedComps = new ArrayList<AchievedCompetenceData>();
		
		if (this.completedAchievedComps != null) {
			this.completedComps.addAll(completedAchievedComps);
		}
		
		List<AchievedCompetenceData> completedActiveCompetences = learningGoalsBean.getCompletedCompetences();
		
		if (completedActiveCompetences != null) {
			this.completedComps.addAll(0, completedActiveCompetences);
		}
		
		Collections.sort(this.completedComps);
	}

	public void initExternalCredits() {
		if (externalCredits == null) {
			logger.debug("Initializing external credits for user "+loggedUser.getUser());
			
			this.externalCredits = externalCreditsDataConverter.convertExternalCredits(
					portfolioManager.getExternalCredits(loggedUser.refreshUser()),
					loggedUser.getUser(),
					loggedUser.getLocale());
		}
	}
	
	public void initSocialNetworks() {
		if (socialNetworksData == null) {
			logger.debug("Initializing social networks data for user "+loggedUser.getUser());
			
			UserSocialNetworks socialNetworks = socialNetworksManager.getSocialNetworks(loggedUser.getUser());
			
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
	
	/*
	 * ACTIONS
	 */
	public void updateSocialNetworks() {
		logger.debug("Updating social networks data for user "+loggedUser.getUser());
		
		saveSocialNetworks();
		
		PageUtil.fireSuccessfulInfoMessage("socialNetworksSettingsForm:socialNetworksFormGrowl", "Social networks updated!");
		
		SettingsBean settings = PageUtil.getViewScopedBean("settings", SettingsBean.class);
		
		if (settings != null) {
			String originLink = settings.getOrigin();
			
			if (originLink != null && originLink.length() > 0)
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(originLink);
				} catch (IOException e) {
					logger.error(e);
				}
		}
	}

	public void saveSocialNetworks() {
		UserSocialNetworksChanged userSocialNetworksChanged = new UserSocialNetworksChanged();
		
		userSocialNetworksChanged = updateSocialNetworkAccount(
				userSocialNetworksChanged, 
				SocialNetworkName.TWITTER,
				socialNetworksData.getTwitterLink(),
				socialNetworksData.getTwitterLinkEdit());
		
		socialNetworksData.setTwitterLink(socialNetworksData.getTwitterLinkEdit());
		
		userSocialNetworksChanged = updateSocialNetworkAccount(
				userSocialNetworksChanged, 
				SocialNetworkName.FACEBOOK,
				socialNetworksData.getFacebookLink(),
				socialNetworksData.getFacebookLinkEdit());

		socialNetworksData.setFacebookLink(socialNetworksData.getFacebookLinkEdit());
		
		userSocialNetworksChanged = updateSocialNetworkAccount(
				userSocialNetworksChanged, 
				SocialNetworkName.GPLUS,
				socialNetworksData.getGplusLink(),
				socialNetworksData.getGplusLinkEdit());

		socialNetworksData.setGplusLink(socialNetworksData.getGplusLinkEdit());
		
		userSocialNetworksChanged = updateSocialNetworkAccount(
				userSocialNetworksChanged, 
				SocialNetworkName.BLOG,
				socialNetworksData.getBlogLink(),
				socialNetworksData.getBlogLinkEdit());

		socialNetworksData.setBlogLink(socialNetworksData.getBlogLinkEdit());
		
		if (userSocialNetworksChanged.changed) {
			userSocialNetworksChanged.userSocialNetworks = socialNetworksManager.saveEntity(userSocialNetworksChanged.userSocialNetworks);
			
			try {
				eventFactory.generateEvent(EventType.UpdatedSocialNetworks, loggedUser.getUser());
			} catch (EventException e) {
				logger.error(e);
			}
		}
	}

	private UserSocialNetworksChanged updateSocialNetworkAccount(UserSocialNetworksChanged userSocialNetworksChanged, 
			SocialNetworkName socialNetworkName, String originalLink, String editedLink) {
		
		if (!originalLink.equals(editedLink)) {
			
			if (userSocialNetworksChanged.userSocialNetworks == null) {
				userSocialNetworksChanged.userSocialNetworks = socialNetworksManager.getSocialNetworks(socialNetworksData.getId());
			}
			
			SocialNetworkAccount account = userSocialNetworksChanged.userSocialNetworks.getAccount(socialNetworkName);
			
			if (account != null) {
				userSocialNetworksChanged.userSocialNetworks = socialNetworksManager.updateSocialNetwork(
						userSocialNetworksChanged.userSocialNetworks, 
						socialNetworkName, 
						editedLink);
			} else {
				account = socialNetworksManager.createSocialNetworkAccount(
						socialNetworkName,
						editedLink);
				
				userSocialNetworksChanged.userSocialNetworks.addSocialNetworkAccount(account);
			}
			
			userSocialNetworksChanged.changed = true;
		}
		return userSocialNetworksChanged;
	}
	
	public void sendToGoals() {
		try {
			TargetLearningGoal retakenGoal = portfolioManager.sendBackToGoals(
					goalDataToBeSentToGoals.getTargetGoalId(), 
					loggedUser.refreshUser(),
					contextSendToGoals);
			
			logger.debug("User \"" + loggedUser.getUser() +
					" send back to goals instance of TargetLearningGoal \""+goalDataToBeSentToGoals.getTitle()+"\" ("+goalDataToBeSentToGoals.getTargetGoalId()+")");
			PageUtil.fireSuccessfulInfoMessage("Goal '"+goalDataToBeSentToGoals.getTitle()+"' is sent back for learning!");
			
			// update cache
			Iterator<GoalData> iterator = this.completedArchivedGoals.iterator();
			
			while (iterator.hasNext()) {
				GoalData goalData = (GoalData) iterator.next();
				
				if (goalData instanceof CompletedGoalData) {
					CompletedGoalData completedGoalData = (CompletedGoalData) goalData;
						
					if (completedGoalData.getTargetGoalId() == goalDataToBeSentToGoals.getTargetGoalId()) {
						iterator.remove();
						break;
					}
				}
			}
			
			GoalDataCache goalData = learningGoalsBean.getData().addGoal(loggedUser.getUser(), retakenGoal);
			
			if (learningGoalsBean.getSelectedGoalData() == null) {
				learningGoalsBean.setSelectedGoalData(goalData);
			}
			
			initCompletedGoals();
			populateWithActiveCompletedGoals();
			
			//initCompletedGoals();
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	initGoalStatistics();
	            }
			});
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public BaseEntity returnEvaluatedResource() {
		if (evaluationParameter > 0) {
			try {
				Evaluation ev = evaluationManager.loadResource(Evaluation.class, evaluationParameter);

				if (ev != null) {
					evaluationParameter = 0;
					return ev.getResource();
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
		return null;
	}
	
	/*
	 * Related to external credit edit
	 */
	
	// add activity to external credit
	public void createNewPost() {
		ActivityWallData newActivityData = createNewActivity(this.newActivityForExCreditData);
		
		if (newActivityData != null) {
			editedExternalCredit.addActivity(newActivityData);
			this.newActivityForExCreditData = new NewPostData();
		}
	}
	
	public ActivityWallData createNewActivity(NewPostData newActivityData) {
		//clean html from text
		String cleanedText = StringUtil.cleanHtml(newActivityData.getText());
				
		try {
			TargetActivity newActivity = resourceFactory.createNewTargetActivity(
					loggedUser.getUser(),
					cleanedText,
					null,
					newActivityData.getAttachmentPreview(), 
					newActivityData.getVisibility(),
					null,
					true);
			eventFactory.generateEvent(EventType.Create, loggedUser.getUser(), newActivity);
			
			newActivity.setCompleted(true);
			newActivity = resourceFactory.saveEntity(newActivity);
			
			Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			ActivityWallData activityData = compWallActivityConverter.convertTargetActivityToActivityWallData(
					null,
					newActivity, 
					loggedUser.getUser(), 
					locale, 
					false, 
					false);
			
			return activityData;
		} catch (EventException e) {
			logger.error(e);
		}
		return null;
	}
	
	public ActivityWallData addActivity(Activity activity) {
		TargetActivity newActivity = resourceFactory.createNewTargetActivity(activity, loggedUser.getUser());
		
		newActivity.setCompleted(true);
		newActivity = resourceFactory.saveEntity(newActivity);
		
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		ActivityWallData activityData = compWallActivityConverter.convertTargetActivityToActivityWallData(
				null,
				newActivity, 
				loggedUser.getUser(), 
				locale, 
				false, 
				false);
		
		return activityData;
	}
	
	// Called from the activity search 
	public void connectActivity(Activity activity) {
		ActivityWallData newAct = addActivity(activity);
		
		if (newAct != null) {
			editedExternalCredit.addActivity(newAct);
		}
	}
	
	// used for activity search
	public String getAllActivitiesIds() {
		return ActivityUtil.extractActivityIds(editedExternalCredit.getActivities());
	}
	
	private ActivityWallData activityToDelete;
	
	public void removeActivity(){
		editedExternalCredit.removeActivity(activityToDelete);
	}

	public void handleFileUpload(FileUploadEvent event) {
    	UploadedFile uploadedFile = event.getFile();
    	
		try {
			AttachmentPreview attachmentPreview = uploadManager.uploadFile(loggedUser.getUser(), uploadedFile, uploadedFile.getFileName());
			
			newActivityForExCreditData.setAttachmentPreview(attachmentPreview);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			
			PageUtil.fireErrorMessage("The file was not uploaded!");
		}
    }
	
	private UploadedFile newCertificateToUpload = null;
	private boolean noCertificate;
	
	public void uploadNewCertificate(FileUploadEvent event) {
		this.newCertificateToUpload = event.getFile();
		this.noCertificate = false;
	}
	
	public void resetExCreditAddActivityForm() {
		this.newCertificateToUpload = null;
		this.editedExternalCredit = null;
		this.newActivityForExCreditData = new NewPostData();
	}
	
	/*
	 * Invoked from the competence search 
	 */
	public void connectCompetence(Competence competence) {
		editedExternalCredit.addCompetence(competence);
	}
	
	public void deleteExternalCredit() {
		if (exCreditDataToDelete != null) {
			logger.debug("User '"+loggedUser.getUser()+"' deleted external credit "+exCreditDataToDelete.getTitle());
			
			try {
				externalCredits = externalCreditsDataConverter.convertExternalCredits(
					portfolioManager.deleteExternalCredit(loggedUser.refreshUser(), exCreditDataToDelete.getExternalCredit(), "profile.credits"),
					loggedUser.getUser(),
					loggedUser.getLocale());
			} catch (Exception e) {
				logger.error("Error with deleting external credit: "+e);
			}
			
			PageUtil.fireSuccessfulInfoMessage("externalCreditsGrowl", "External credit deleted!");
		}
	}
	
	public void saveExternalCreditEdits() {
		int index = externalCredits.indexOf(editedExternalCredit);
		
		Iterator<ExternalCreditData> iterator = this.externalCredits.iterator();
		
		while (iterator.hasNext()) {
			ExternalCreditData exCreditData = (ExternalCreditData) iterator.next();
			
			if (exCreditData.equals(editedExternalCredit)) {
				iterator.remove();
				break;
			}
		}
		
		String newCertificateLink = null;
		if (newCertificateToUpload != null) {
			try {
				newCertificateLink = uploadManager.storeFile(
						loggedUser.getUser(), 
						newCertificateToUpload, 
						newCertificateToUpload.getFileName());
			} catch (IOException e) {
				logger.error("Error saving new certificate for External credit '"+editedExternalCredit+"' being edited. "+e);
			}
		}

		List<TargetActivity> activities = new ArrayList<TargetActivity>();
		
		for (ActivityWallData actWall : editedExternalCredit.getActivities()) {
			try {
				TargetActivity activity = userManager.loadResource(TargetActivity.class, actWall.getObject().getId());
				activities.add(activity);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
		
		ExternalCredit updatedExCredit = portfolioManager.updateExternalCredit(
				editedExternalCredit.getExternalCredit(),
				loggedUser.getUser(),
				editedExternalCredit.getTitle(),
				editedExternalCredit.getDescription(),
				editedExternalCredit.getStart(),
				editedExternalCredit.getEnd(),
				noCertificate ? editedExternalCredit.getCertificateLink() : newCertificateLink,
				editedExternalCredit.getCompetences(),
				activities);
		
		externalCredits.add(index, externalCreditsDataConverter.convertExternalCredit(
				updatedExCredit, 
				loggedUser.getUser(),
				loggedUser.getLocale()));
		
		PageUtil.fireSuccessfulInfoMessage("externalCreditsGrowl", "External credit edited!");
	}
	
	// Status Wall
	public void initializeActivities() {
		logger.debug("Initializing portfolio activity wall");
		
		portfolioActivitiesDisplayer.initializeActivities();
		logger.debug("Initialized portfolio activity wall");
	}
	
	public void refresh() {
		
	}
	
	public void loadMoreActivities() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", "portfolio");
		parameters.put("link", "loadMore");
		
		portfolioActivitiesDisplayer.loadMoreActivities(parameters);
	}
	
	public List<SocialActivityData> getAllActivities() {
		return portfolioActivitiesDisplayer.getAllActivities();
	}
	
	public boolean isMoreToLoad() {
		return portfolioActivitiesDisplayer.isMoreToLoad();
	}
	
	/*
	 * PARAMETERS
	 */
	private long evaluationParameter;
	
	public long getEvaluationParameter() {
		return evaluationParameter;
	}
	
	public void setEvaluationParameter(long evaluationParameter) {
		this.evaluationParameter = evaluationParameter;
	}
	
	/*
	 * GETTERS/SETTERS
	 */

	public List<GoalData> getCompletedGoals() {
		return completedGoals;
	}

	public List<AchievedCompetenceData> getCompletedAchievedComps() {
		return completedAchievedComps;
	}
	
	public List<ExternalCreditData> getExternalCredits() {
		return externalCredits;
	}
	
	private String contextSendToGoals;

	public void setGoalDataToBeSentToGoals(final GoalData goalDataToBeSentToGoals, final String context) {
		this.goalDataToBeSentToGoals = goalDataToBeSentToGoals;
		
		this.contextSendToGoals = context;
		
		if (goalDataToBeSentToGoals != null) {
			taskExecutor.execute(new Runnable() {
			    @Override
			    public void run() {
			    	loggingNavigationBean.logServiceUse(
			    			ComponentName.SEND_TO_GOALS_DIALOG, 
			    			"context", context,
			    			"targetGoalId", String.valueOf(goalDataToBeSentToGoals.getTargetGoalId()));
			    }
			});
		}
	}
	
	public GoalData getGoalDataToBeSentToGoals() {
		return goalDataToBeSentToGoals;
	}

	public String getContextSendToGoals() {
		return contextSendToGoals;
	}

	public GoalStatisticsData getGoalStats() {
		return goalStats;
	}

	public NewPostData getNewActivityForExCreditData() {
		return newActivityForExCreditData;
	}

	public ExternalCreditData getEditedExternalCredit() {
		return editedExternalCredit;
	}
	
	public void setEditedExternalCredit(ExternalCreditData editedExternalCredit) {
		this.editedExternalCredit = editedExternalCredit;
		this.noCertificate = editedExternalCredit.getCertificateLink() == null;
	}
	
	public ActivityWallData getActivityToDelete() {
		return activityToDelete;
	}

	public void setActivityToDelete(ActivityWallData activityToDelete) {
		this.activityToDelete = activityToDelete;
	}

	public UploadedFile getNewCertificateToUpload() {
		return newCertificateToUpload;
	}

	public void setNewCertificateToUpload(UploadedFile newCertificateToUpload) {
		this.newCertificateToUpload = newCertificateToUpload;
	}

	public boolean isNoCertificate() {
		return noCertificate;
	}

	public void setNoCertificate(boolean noCertificate) {
		this.noCertificate = noCertificate;
	}

	public ExternalCreditData getExCreditDataToDelete() {
		return exCreditDataToDelete;
	}

	public void setExCreditDataToDelete(ExternalCreditData exCreditDataToDelete) {
		this.exCreditDataToDelete = exCreditDataToDelete;
		
		loggingNavigationBean.logServiceUse(
				ComponentName.DELETE_EXTERNAL_CREDIT_DIALOG, 
				"context", "profile.credits."+exCreditDataToDelete.getId(),
				"exCreditId", String.valueOf(exCreditDataToDelete.getId()));
	}

	public SocialNetworksData getSocialNetworksData() {
		if(socialNetworksData==null){
			this.initSocialNetworks();
		}
		return socialNetworksData;
	}

	public List<GoalData> getCompletedArchivedGoals() {
		return completedArchivedGoals;
	}

	public List<AchievedCompetenceData> getCompletedComps() {
		initAchievedCompetences();
		return completedComps;
	}
	
	
}

class UserSocialNetworksChanged {
	UserSocialNetworks userSocialNetworks;
	boolean changed;
}