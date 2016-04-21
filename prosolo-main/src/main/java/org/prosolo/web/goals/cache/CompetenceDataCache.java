/**
 * 
 */
package org.prosolo.web.goals.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.evaluation.Badge;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.workflow.LearningPlan;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.recommendation.ActivityRecommender;
import org.prosolo.recommendation.LearningPlanRecommendation;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.data.LastActivityAware;
import org.prosolo.web.goals.data.TargetCompetenceData;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 * 
 */
@Service
@Scope("prototype")
public class CompetenceDataCache implements Serializable, LastActivityAware, Comparable<CompetenceDataCache> {

	private static final long serialVersionUID = -7794554780666077552L;
	
	private static Logger logger = Logger.getLogger(LearnBean.class);
	
	@Autowired private CompetenceManager compManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompWallActivityConverter compWallActivityConverter;
	
	@Autowired private LearningPlanRecommendation lpRecommender;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;
	@Autowired private ActivityRecommender activityRecommender;

	private TargetCompetenceData data;
	private boolean completed;
	private List<ActivityWallData> activities;
	private GoalDataCache parentGoalDataCache;
	
	private List<LearningPlan> recommendLearningPlans;

	private List<Evaluation> evaluations;
	private long evaluationCount = 0;
	private long rejectedEvaluationCount = 0;
	private boolean evaluationCountInitialised = false;
	
	private List<Badge> badges;
	private long badgeCount = 0;
	private boolean badgeCountInitialised = false;
	
	private boolean showRecommendedActivities;
	private List<ActivityWallData> recommendedActivities; 
	
	
	public void init(TargetCompetence comp) {
		this.data = new TargetCompetenceData(comp);
		
//		Date lastActivity = ServiceLocator.getInstance().getService(ActivityWallManager.class).getLastActionForTargetCompetence(comp, loggedUser.getUser());
//		this.data.setLastActivity(lastActivity);
		this.completed = comp.isCompleted();
		this.showRecommendedActivities = !comp.isHideActivityRecommendations();
	}

	public void init(GoalDataCache parentGoalDataCache, TargetCompetence comp) {
		init(comp);
		this.parentGoalDataCache = parentGoalDataCache;
		this.data.setTargetGoalId(parentGoalDataCache.getData().getTargetGoalId());
		this.showRecommendedActivities = !comp.isHideActivityRecommendations();
		calculateCanNotBeMarkedAsCompleted();
	}
	
	public void calculateCanNotBeMarkedAsCompleted() {
		initializeActivities();
		
		if (activities != null) {
			for (ActivityWallData actData : activities) {
				
				if (actData.getActivity().getClazz().equals(UploadAssignmentActivity.class) && 
						!actData.isCompleted()) {
					data.setCanNotBeMarkedAsCompleted(ActionDisabledReason.COMPLETION_DISABLED_UPLOAD_ACTIVITY_LEFT_UNCOMPLETED);
					return;
				}
			}
			data.setCanNotBeMarkedAsCompleted(null);
		}
	}
	
	public void initializeActivities() {
		if (activities == null) {
			logger.debug("Initialize activities");
			List<TargetActivity> sortedActivities = compManager.getTargetActivities(data.getId());
			
			this.activities = compWallActivityConverter.convertToActivityInstances(
					this,
					sortedActivities, 
					loggedUser.getUser(), 
					true, 
					false,
					loggedUser.getLocale());
		}
	}

	public void addActivity(ActivityWallData actData) {
		this.activities.add(0, actData);
	}
	
	public boolean containsActivity(Activity activity) throws ResourceCouldNotBeLoadedException {
		// initialize activities if already not initialized
		initializeActivities();

		for (ActivityWallData actData : activities) {
			if (actData.getActivity().getId() == activity.getId()) {
				return true;
			}
		}
		return false;
	}
	
	public ActivityWallData getActivity(long targetActivityId) {
		for (ActivityWallData activityWallData : activities) {
			if (activityWallData.getId() == targetActivityId) {
				return activityWallData;
			}
		}
		return null;
	}
	
	// RECOMMENDED LEARNING PLANS
	@Deprecated
	public void initializeRecommendedLearningPlans() {
		logger.debug("Initialize CompetenceDataCache");
		
		if (recommendLearningPlans == null) {
			recommendLearningPlans = lpRecommender.recommendLearningPlans(
				loggedUser.getUser(), 
				data.getId(), 
				4);
		}
	}
	
	public void markActivitiesAsAdded(List<TargetActivity> targetActivities) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean hasAppendedPlans(AvailableLearningPlan alearningPlan){
		return lpRecommender.hasAppendedPlansForCompetence(alearningPlan.getPlan(), this.data.getCompetenceId());
	}
	
	/*
	 * Recommended Activities - Shown atop of the Activities list
	 */
	
	public void disableActivitiesRecommendation() {
		boolean disabled = compManager.disableActivityRecommendations(this.data.getId());
		
		if (disabled) {
			this.showRecommendedActivities = false;
			PageUtil.fireSuccessfulInfoMessage("Activity recommendations are disabled");
		} else {
			PageUtil.fireErrorMessage("There were problems disabling activity recommendations");
		}
	}
	
	/*
	 * Evaluations
	 */
	public void initializeEvaluations() {
		if (evaluations == null) {
			//evaluations = evaluationManager.getEvaluationsForResource(TargetCompetence.class, data.getId());
		}
		if (!evaluationCountInitialised) {
			evaluationCount = evaluationManager.getApprovedEvaluationCountForResource(TargetCompetence.class, data.getId());
			evaluationCountInitialised = true;
		}
	}	
	
	public List<Long> getEvaluationIds() {
		if (evaluations != null) {
			List<Long> ids = new ArrayList<Long>();
			
			for (Evaluation ev : evaluations) {
				ids.add(ev.getId());
			}
			return ids;
		}
		return new ArrayList<Long>();
	}
	
	/*
	 * Badges
	 */
	public void initializeBadges() {
		if (!badgeCountInitialised) {
			badgeCount = badgeManager.getBadgeCountForResource(TargetCompetence.class, data.getId());
			badgeCountInitialised = true;
		}
	}
	
	/*
	 * Activities
	 */
	public boolean updateActivityOutcome(long targetActivityId, Outcome outcome) {
		for (ActivityWallData actData : getActivities()) {
			
			if (actData.getId() == targetActivityId) {
				
				if (outcome instanceof SimpleOutcome) {
					actData.setResult(((SimpleOutcome) outcome).getResult());
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Related Activities
	 */
	
	public List<ActivityWallData> retrieveRelatedActivities(ActivityWallData selectedActivitiy) {
		if (selectedActivitiy.getRelatedActivities() == null) {
			List<Activity> relatedActivities = activityRecommender.getRelatedActivities(
					data.getCompetenceId(), 
					selectedActivitiy.getActivity().getId(), 
					2);
			
			if (relatedActivities != null && !relatedActivities.isEmpty()) {
				List<ActivityWallData> convertedRelatedActivities = compWallActivityConverter.convertActivities(
						relatedActivities,
						loggedUser.getUser(),
						loggedUser.getLocale(),
						true, 
						false);
				selectedActivitiy.setRelatedActivities(convertedRelatedActivities);
			}
		}
		return selectedActivitiy.getRelatedActivities();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public boolean isCompleted() {
		return completed;
	}

	public TargetCompetenceData getData() {
		return data;
	}

	public void setData(TargetCompetenceData data) {
		this.data = data;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public List<ActivityWallData> getActivities() {
		initializeActivities();
		return activities;
	}

	public void setActivities(List<ActivityWallData> activities) {
		this.activities = activities;
	}
	
	public List<LearningPlan> getRecommendLearningPlans() {
		return recommendLearningPlans;
	}

	public void setRecommendLearningPlans(List<LearningPlan> recommendLearningPlans) {
		this.recommendLearningPlans = recommendLearningPlans;
	}
	
	public GoalDataCache getParentGoalDataCache() {
		return parentGoalDataCache;
	}
	
	public List<Evaluation> getEvaluations() {
		initializeEvaluations();
		return evaluations;
	}
	
	public long getEvaluationCount() {
		initializeEvaluations();
		return evaluationCount;
	}

	public void setEvaluationCount(long evaluationCount) {
		this.evaluationCount = evaluationCount;
	}
	
	public long getRejectedEvaluationCount() {
		return rejectedEvaluationCount;
	}

	public void setRejectedEvaluationCount(long rejectedEvaluationCount) {
		this.rejectedEvaluationCount = rejectedEvaluationCount;
	}

	public List<Badge> getBadges() {
		initializeBadges();
		return badges;
	}

	public long getBadgeCount() {
		initializeBadges();
		return badgeCount;
	}
	
	public void setBadgeCount(long badgeCount) {
		this.badgeCount = badgeCount;
	}

	public boolean isBadgeCountInitialised() {
		return badgeCountInitialised;
	}

	@Override
	public int compareTo(CompetenceDataCache c) {
		return this.getData().getTitle().compareTo(c.getData().getTitle());
	}
	
	@Override
	public String toString() {
		return this.getData().getTitle();
	}

	@Override
	public Date getLastActivity() {
		return data.getLastActivity();
	}

	@Override
	public void setLastActivity(Date lastActivity) {
		data.setLastActivity(lastActivity);
	}

	public boolean isShowRecommendedActivities() {
		return showRecommendedActivities;
	}

	public List<ActivityWallData> getRecommendedActivities() {
		return recommendedActivities;
	}

	public void setRecommendedActivities(List<ActivityWallData> recommendedActivities) {
		this.recommendedActivities = recommendedActivities;
	}
	
}
