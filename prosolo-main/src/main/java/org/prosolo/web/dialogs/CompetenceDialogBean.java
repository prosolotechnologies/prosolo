package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.omnifaces.util.Ajax;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.recommendation.LearningPlanRecommendation;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.FollowResourceAsyncManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.dialogs.data.CompetenceFormData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.competences.CompetenceAnalyticsBean;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.data.CompetenceAnalyticsData;
import org.prosolo.web.goals.data.CompetenceData;
import org.prosolo.web.goals.util.AvailableLearningPlanConverter;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.useractions.DislikeActionBean;
import org.prosolo.web.useractions.LikeActionBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="competenceDialog")
@Component("competenceDialog")
@Scope("view")
public class CompetenceDialogBean implements Serializable {
	
	private static final long serialVersionUID = -3718204445093106338L;
	
	protected static Logger logger = Logger.getLogger(CompetenceDialogBean.class);

	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompetenceAnalyticsBean compAnalyticsBean;
	@Autowired private LearningGoalsBean goalBeans;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private LikeActionBean likeAction;
	@Autowired private DislikeActionBean dislikeAction;
	@Autowired private LearningPlanRecommendation lpRecommender;
	@Autowired private AvailableLearningPlanConverter availableLearningPlanConverter;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	@Autowired private TagManager tagManager;
	@Autowired private LearningGoalsBean goalsBean;
	@Autowired private FollowResourceAsyncManager followResourceAsyncManager;
	@Autowired private EventFactory eventFactory;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private CompetenceDialogMode mode;
	private Competence competence;
	private TargetCompetence targetCompetence;
	private List<AvailableLearningPlan> recommendedPlans;
	private List<ActivityWallData> usedActivities;
	private List<ActivityWallData> predefinedActivities;
	
	private CompetenceFormData formData = new CompetenceFormData();
	private boolean canBeConnected;
	private String dialogTitle = "Create new competence";
	
	private boolean owner;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	private void initializeFormData() {
		competence = competenceManager.merge(competence);
		
		if (!Hibernate.isInitialized(competence.getTags())) {
			competence = competenceManager.merge(competence);
		}
		
		fillFormData();
		
		this.recommendedPlans = null;
		
		switch (mode) {
			case TARGET_COMPETENCE:
				Locale locale = loggedUser != null ? loggedUser.getLocale() : new Locale("en", "US");
				usedActivities = compWallActivityConverter.generateCompWallActivitiesData(targetCompetence, locale);
				break;
			case RECOMMENDED_COMPETENCE:
				initializeRecommendedPlans();
				break;
			default:
				User user = loggedUser != null ? loggedUser.getUser() : null;
				predefinedActivities = compWallActivityConverter.convertCompetenceActivities(competence.getActivities(), user, true, false);
				break;
		}
		
		this.canBeConnected = true;
	}

	public void fillFormData() {
		formData = new CompetenceFormData(competence);
		formData.setAnalyticsData(compAnalyticsBean.createAnalyticsData(competence.getId()));
		
		User user = loggedUser != null ? loggedUser.getUser() : null;
		
		if (user != null) {
			formData.setLikedByUser(likeManager.isLikedByUser(competence, user));
			formData.setDislikedByUser(dislikeManager.isDislikedByUser(competence, user));
			formData.setOwnedByUser(competenceManager.hasUserCompletedCompetence(competence, user));
		}
		formData.setCanEdit(user != null && (loggedUser.hasRole("MANAGER") || user.getId() == competence.getMaker().getId()));
		
		this.owner = loggedUser.isLoggedIn() && loggedUser.getUser().getId() == competence.getMaker().getId();
	}

	public void initializeRecommendedPlans() {
		if (recommendedPlans == null) {
			
			User user = loggedUser != null ? loggedUser.getUser() : null;
			
			recommendedPlans = availableLearningPlanConverter.packIntoAvailablePlan(
					user,
					null,
					lpRecommender.recommendLearningPlansForCompetence(
							user,
							competence, 
							4));
		}
	}
	
	public boolean hasSetCompetence(){
		return competence != null;
	}
	
	/**
	 * saving / updating competence changes
	 */
	public void saveCompetence(){
		if (competence == null) {
			String title = StringUtil.cleanHtml(formData.getTitle());
			try {
				competence = competenceManager.createCompetence(
						loggedUser.getUser(),
						title,
						StringUtil.cleanHtml(formData.getDescription()),
						formData.getValidity(),
						formData.getDuration(),
						new HashSet<Tag>(tagManager.parseCSVTagsAndSave(formData.getTagsString())),
						formData.getPrerequisites(),
						formData.getCorequisites());

				PageUtil.fireSuccessfulInfoMessage("newCompFormGrowl", "Competence '"+title+"' is created!");
			} catch (EventException e) {
				logger.error(e);
			}
		} else {
			boolean titleChanged = !competence.getTitle().equals(formData.getTitle());
			
			competence = competenceManager.updateCompetence(
				competence,
				formData.getTitle(),
				formData.getDescription(),
				formData.getDuration(),
				formData.getValidity(),
				new HashSet<Tag>(tagManager.parseCSVTagsAndSave(formData.getTagsString())),
				competence.getCorequisites(),
				competence.getPrerequisites(),
				null,
				false
			);
				
			try {
				eventFactory.generateEvent(EventType.Edit, loggedUser.getUser(), competence);
			} catch (EventException e1) {
				logger.error(e1);
			}
			
			PageUtil.fireSuccessfulInfoMessage(null, "Competence data updated!");
			
			if (titleChanged) {
				// update competence's name in cache everywhere on the interface
				updateCompNameInCache(competence, goalBeans);
				Ajax.update("competenceList", "selectedCompetenceDetailsForm");
				
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						
						// if competence is updated from the goals page, then we need to update competence data everywhere on that page
					}
				});
			}
		}
	}
	
	/**
	 * @param learningGoalsBean TODO
	 * @param competence2
	 */
	private void updateCompNameInCache(Competence competence, LearningGoalsBean learningGoalsBean) {
		if (learningGoalsBean != null && learningGoalsBean.getData().getGoals() != null) {
			String newTitle = competence.getTitle();
			long compeId = competence.getId();
			
			
			for (GoalDataCache goalCacheData : learningGoalsBean.getData().getGoals()) {
				if (goalCacheData.getCompetences() != null) {
					
					for (CompetenceDataCache compCacheData : goalCacheData.getCompetences()) {
						if (compCacheData.getData().getCompetenceId() == compeId)
							compCacheData.getData().setTitle(newTitle);
					}
				}
				if (goalCacheData.getRecommendedCompetences() != null) {
					
					for (CompetenceData compData : goalCacheData.getRecommendedCompetences()) {
						if (compData.getId() == compeId)
							compData.setTitle(newTitle);
					}
				}
			}
		}
	}

	public void cancelCompetenceEdit(){
		fillFormData();
	}
	
	public void reset() {
		formData = new CompetenceFormData();
		this.competence = null;
		this.targetCompetence = null;
	}
	
	public void startFollowingResource(Competence comp){
		formData.setFollowedByUser(true);
		followResourceAsyncManager.asyncFollowResource(loggedUser.getUser(), comp, generateContext());
	}
	
	public void stopFollowingResource(Competence comp){
		formData.setFollowedByUser(false);
		followResourceAsyncManager.asyncUnfollowResource(loggedUser.getUser(), comp, generateContext());
	}
	
	public void like(final long compId){
		formData.setLikedByUser(true);
		CompetenceAnalyticsData compAnalyticsData = compAnalyticsBean.recalculateLikes(compId, formData.getAnalyticsData().getNumOfLikes()+1);
		
		if (compAnalyticsData == null) {
			logger.error("Could not find analytics data for competence with id: "+compId);
			return;
		}
		
		formData.setAnalyticsData(compAnalyticsData);
		
		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	Session session = (Session) portfolioManager.getPersistence().openSession();
            	
            	try {
					likeAction.like(compId, Competence.class, session, generateContext());
					session.flush();
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
            	 
             finally{
 				HibernateUtil.close(session);
 			} 
            }
        });
	}
	
	public void removeLike(final long compId){
		formData.setLikedByUser(false);
		CompetenceAnalyticsData compAnalyticsData = compAnalyticsBean.recalculateLikes(compId, formData.getAnalyticsData().getNumOfLikes()-1);
		
		if (compAnalyticsData == null) {
			logger.error("Could not find analytics data for competence with id: "+compId);
			return;
		}
		
		formData.setAnalyticsData(compAnalyticsData);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) portfolioManager.getPersistence().openSession();
				
				try {
					likeAction.removeLike(compId, Competence.class, session, generateContext());
					session.flush();
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
			
				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
	}
	
	public void dislike(final long compId){
		formData.setDislikedByUser(true);
		CompetenceAnalyticsData compAnalyticsData = compAnalyticsBean.recalculateDislikes(compId, formData.getAnalyticsData().getNumOfDislikes()+1);

		if (compAnalyticsData == null) {
			logger.error("Could not find analytics data for competence with id: "+compId);
			return;
		}

		formData.setAnalyticsData(compAnalyticsData);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) portfolioManager.getPersistence().openSession();
				
				try {
					dislikeAction.dislike(compId, Competence.class, session, generateContext());
					session.flush();
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
				
			
				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
	}
	
	public void removeDislike(final long compId){
		formData.setDislikedByUser(false);
		CompetenceAnalyticsData compAnalyticsData = compAnalyticsBean.recalculateDislikes(compId, formData.getAnalyticsData().getNumOfDislikes()-1);
		
		if (compAnalyticsData == null) {
			logger.error("Could not find analytics data for competence with id: "+compId);
			return;
		}

		formData.setAnalyticsData(compAnalyticsData);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) portfolioManager.getPersistence().openSession();
				
				try {
					dislikeAction.removeDislike(compId, Competence.class, session, generateContext());
					
					session.flush();
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			

				 finally{
		 				HibernateUtil.close(session);
		 			} 
			}
		});
	}
	
	public String generateContext() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("competenceDialog.");
		
		if (targetCompetence != null) {
			buffer.append("targetComp." + targetCompetence.getId());
		} else if (competence != null){
			buffer.append("comp." + competence.getId());
		}
		
		return buffer.toString();
	}
	
	public void addPrerequisite(Competence competence) {
		if (competence != null) {
			formData.getPrerequisites().add(competence);
		}
	}
	
	public void removePrerequisite(Competence competence) {
		if (competence != null && formData.getPrerequisites().contains(competence)) {
			Iterator<Competence> iterator = formData.getPrerequisites().iterator();
			
			while (iterator.hasNext()) {
				Competence c = (Competence) iterator.next();
				
				if (competence.equals(c)) {
					iterator.remove();
					break;
				}
			}
		}
	}
	
	public void addCorequisite(Competence competence) {
		if (competence != null) {
			formData.getCorequisites().add(competence);
		}
	}
	
	public void removeCorequisite(Competence competence) {
		if (competence != null && formData.getCorequisites().contains(competence)) {
			Iterator<Competence> iterator = formData.getCorequisites().iterator();
			
			while (iterator.hasNext()) {
				Competence c = (Competence) iterator.next();
				
				if (competence.equals(c)) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	public Competence getCompetence() {
		return competence;
	}
	
	public void setCompetenceId(long competenceId) {
		try {
			reset();
			Competence comp = competenceManager.loadResource(Competence.class, competenceId);
			setCompetence(comp);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void setCompetence(Competence competence) {
		reset();
		this.competence = competence;
		mode = CompetenceDialogMode.RECOMMENDED_COMPETENCE;
		initializeFormData();
		this.dialogTitle = "Edit competence "+formData.getTitle(); 
	}
	
	public void setCourseCompetence(long courseCompetenceId) {
		reset();
		try {
			CourseCompetence courseComp = competenceManager.loadResource(CourseCompetence.class, courseCompetenceId);
			this.competence = courseComp.getCompetence();
			mode = CompetenceDialogMode.RECOMMENDED_COMPETENCE;
			initializeFormData();
			this.dialogTitle = "Competence "+formData.getTitle(); 
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public TargetCompetence getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetenceId(long targetCompetenceId) {
		reset();
		try {
			TargetCompetence tc = competenceManager.loadResource(TargetCompetence.class, targetCompetenceId);
			setTargetCompetence(tc);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void setTargetCompetence(TargetCompetence targetCompetence) {
		reset();
		
		this.targetCompetence = targetCompetence;
		this.competence = targetCompetence.getCompetence();
		mode = CompetenceDialogMode.TARGET_COMPETENCE;
		initializeFormData();
		
		if (goalsBean != null && goalsBean.getSelectedGoalData() != null &&
				goalsBean.getSelectedGoalData().containsCompetence(competence.getId())) {
			canBeConnected = false;
		}
	}
	
	public void setOriginalCompetenceId(long competenceId) {
		reset();
		
		try {
			Competence comp = competenceManager.loadResource(Competence.class, competenceId);
			setOriginalCompetence(comp);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public void setOriginalCompetence(Competence competence) {
		reset();
		
		this.competence = competence;
		mode = CompetenceDialogMode.ORIGINAL_COMPETENCE;
		initializeFormData();
	}
	
	public CompetenceDialogMode getMode() {
		return mode;
	}
	
	public void setMode(CompetenceDialogMode mode) {
		this.mode = mode;
	}
	
	public CompetenceFormData getFormData() {
		return formData;
	}

	public void setFormData(CompetenceFormData formData) {
		this.formData = formData;
	}

	public List<AvailableLearningPlan> getRecommendedPlans() {
		return recommendedPlans;
	}

	public void setRecommendedPlans(List<AvailableLearningPlan> recommendedPlans) {
		this.recommendedPlans = recommendedPlans;
	}

	public List<ActivityWallData> getUsedActivities() {
		return usedActivities;
	}

	public void setUsedActivities(List<ActivityWallData> usedActivities) {
		this.usedActivities = usedActivities;
	}
	
	public String getDialogTitle() {
		return dialogTitle;
	}
	
	public List<ActivityWallData> getPredefinedActivities() {
		return predefinedActivities;
	}

	public boolean isCanBeConnected() {
		return canBeConnected;
	}

	public boolean isOwner() {
		return owner;
	}

	public enum CompetenceDialogMode {
		ORIGINAL_COMPETENCE,
		TARGET_COMPETENCE,
		RECOMMENDED_COMPETENCE
	}
}
