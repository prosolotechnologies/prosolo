package org.prosolo.web.goals;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.workflow.LearningPlan;
import org.prosolo.recommendation.LearningPlanRecommendation;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.data.RecommendedActivityData;
import org.prosolo.web.goals.util.AvailableLearningPlanConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "recommendedplans")
@Component("recommendedplans")
@Scope("session")
public class RecommendedLearningPlansBean {

	private static Logger logger = Logger.getLogger(RecommendedLearningPlansBean.class);

	@Autowired private LearningPlanRecommendation lpRecommender;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private SuggestedLearningQueries suggLearningQueries;
	@Autowired private AppendedActivitiesBean appendedActivitiesBean;
	@Autowired private ActivityManager activityManager;
	@Autowired private AvailableLearningPlanConverter availableLearningPlanConverter;
	
	private TargetCompetence selectedComp;
	// this is used from the interface
	private List<AvailableLearningPlan> recommendedPlans;
	
	// this is used to cache the recommendation
	private List<LearningPlan> recommendLearningPlans;
	
	public void init(long targetCompetenceId) {
		try {
			TargetCompetence targetCompetence = activityManager.loadResource(TargetCompetence.class, targetCompetenceId, true);
			init(targetCompetence);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void init(TargetCompetence targetCompetence) {
		logger.debug("Initializing RecommendedLearningPlansBean");
		this.selectedComp = activityManager.merge(targetCompetence);
		recommendLearningPlans = lpRecommender.recommendLearningPlans(
				loggedUser.getUser(), 
				selectedComp.getId(), 
				4);
		convertToAvailableLearningPlans(this.selectedComp);
		this.appendedActivitiesBean.init(this.selectedComp);
	}

	public void convertToAvailableLearningPlans(TargetCompetence selectedComp) {
		LearningPlan lPlan = new LearningPlan();
		selectedComp=activityManager.merge(selectedComp);
		List<TargetActivity> targetActivities = selectedComp.getTargetActivities();
		List<Activity> activities = new ArrayList<Activity>();
		
		for (TargetActivity tgActivity : targetActivities) {
			activities.add(tgActivity.getActivity());
		}
		lPlan.setActivities(activities);
		this.recommendedPlans = availableLearningPlanConverter.packIntoAvailablePlan(
				loggedUser.getUser(),
				lPlan,
				recommendLearningPlans);
		
		calculateConnectAllFlagForAllPlans();
	}
	
	public void calculateConnectAllFlagForAllPlans() {
		if (!this.recommendedPlans.isEmpty()) {
			for (AvailableLearningPlan plan : this.recommendedPlans) {
				calculateConnectAllFlag(plan);
			}
		}
	}
	
	public void calculateConnectAllFlag(AvailableLearningPlan plan) {
		boolean allActivitiesInCurrentPlan = true;
		
		for (RecommendedActivityData act : plan.getActivities()) {
			allActivitiesInCurrentPlan = allActivitiesInCurrentPlan && act.isContainedInCurrentPlan();
		}
		
		plan.setAllActivitiesInCurrentPlan(allActivitiesInCurrentPlan);
	}

	public boolean doesNotContainActivity(long activityId) {
		 if (selectedComp != null) {
			LearningPlan lPlan = new LearningPlan();
			List<TargetActivity> targetActivities = selectedComp.getTargetActivities();
			List<Activity> activities = new ArrayList<Activity>();
			
			for (TargetActivity tActivity : targetActivities) {
				activities.add(tActivity.getActivity());
			}
			
			lPlan.setActivities(activities);
			//LearningPlan lPlan = selectedComp.getPlan();
//			lPlan = activityManager.merge(lPlan);
			//Collection<TargetActivity> activities = lPlan.getActivities();
			
			for (Activity act : activities) {
				if (activityId == act.getId()) {
					return false;
				}
			}
		} 
		return true;
	}

	// TODO: Refactor - try to put this information in AvailableLearningPlan instance during init
	public boolean hasAppendedPlans(AvailableLearningPlan alearningPlan, TargetCompetence targetCompetence) {
		if (selectedComp == null && targetCompetence != null) {
			init(targetCompetence);
		}
		
		return lpRecommender.hasAppendedPlans(alearningPlan.getPlan(), this.selectedComp);
	}
	
	public boolean hasAppendedPlansForCompetence(LearningPlan learningPlan, Competence competence){
		return lpRecommender.hasAppendedPlansForCompetence(learningPlan, competence.getId());
	}
	
	public void disableAddingActivity(long activityId) {
		if (recommendedPlans != null) {
			for (AvailableLearningPlan lPlan : recommendedPlans) {
				for (RecommendedActivityData act : lPlan.getActivities()) {
					if (act.getActivityId() == activityId) {
						act.setContainedInCurrentPlan(true);
						return;
					}
				}
			}
		}
	}
	
	public void disableAddingActivities(AvailableLearningPlan lPlan) {
		if (recommendedPlans != null) {
			for (AvailableLearningPlan plan : recommendedPlans) {
				if (plan.equals(lPlan)) {
					lPlan = plan;
					break;
				}
			}
			
			for (RecommendedActivityData act : lPlan.getActivities()) {
				act.setContainedInCurrentPlan(true);
			}
		}
	}
	
	public void enableAddingActivity(TargetActivity targetActivity) {
		if (targetActivity != null) {
			for (AvailableLearningPlan lPlan : recommendedPlans) {
				for (RecommendedActivityData act : lPlan.getActivities()) {
					if (act.getActivity().getId() == targetActivity.getActivity().getId()) {
						act.setContainedInCurrentPlan(false);
						return;
					}
				}
			}
		}
	}
	
	public void toggleCompletedActivity(long activityId, boolean completed) {
		if (recommendedPlans != null) {
			for (AvailableLearningPlan lPlan : recommendedPlans) {
				for (RecommendedActivityData act : lPlan.getActivities()) {
					if (act.getActivityId() == activityId) {
						act.setCompletedByUser(completed);
					}
				}
			}
		}
	}

	
	/*
	 * GETTERS / SETTERS
	 */

	public TargetCompetence getSelectedComp() {
		return selectedComp;
	}
	public void setSelectedComp(TargetCompetence tCompetence){ 
		this.selectedComp=tCompetence;
	}

	public List<AvailableLearningPlan> getRecommendedPlans() {
		return recommendedPlans;
	}

}
