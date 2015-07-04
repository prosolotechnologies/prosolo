package org.prosolo.web.goals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.workflow.LearningPlan;
import org.prosolo.recommendation.LearningPlanRecommendation;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.data.AppendedLearningPlanData;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.util.CompWallActivityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * zoran
 */
@ManagedBean(name = "appendedActivitiesBean")
@Component("appendedActivitiesBean")
@Scope("session")
public class AppendedActivitiesBean implements Serializable {

	private static final long serialVersionUID = -9197962691568227899L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AppendedActivitiesBean.class.getName());

	@Autowired private LearningPlanRecommendation lpRecommender;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SuggestedLearningQueries suggLearningQueries;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private CompWallActivityConverter compWallActivityConverter;

	private TargetCompetence selectedTargetCompetence;
	private Competence selectedCompetence;
	private AvailableLearningPlan selectedAvailablePlan;
	
	public void init(TargetCompetence selectedTargetCompetence) {
		this.selectedTargetCompetence = selectedTargetCompetence;
	}

	public void init(Competence selectedCompetence) {
		this.selectedCompetence = selectedCompetence;
	}

	public void initializeSelectedAvailablePlan(AvailableLearningPlan selectedAvailablePlan) {
		this.selectedAvailablePlan = selectedAvailablePlan;
	}

	public void initializeSelectedAvailablePlanAndCompetence(AvailableLearningPlan selectedAvailablePlan, Competence selCompetence) {
		if (selCompetence != null) {
			this.selectedCompetence = selCompetence;
		}
		initializeSelectedAvailablePlan(selectedAvailablePlan);
	}

	public List<AppendedLearningPlanData> getAppendedPlans() {
		
		if(selectedAvailablePlan!=null && selectedAvailablePlan.getPlan()!=null && selectedAvailablePlan.getPlan().getAppendedActivities().size()>0){
			return convertAppendedActivitiesToAppendedLearningPlansData(selectedAvailablePlan.getPlan().getAppendedActivities());
		}else if (selectedTargetCompetence != null && this.selectedAvailablePlan != null) {
			return convertToAppendedLearningPlansData(lpRecommender.getAppendedPlans(selectedAvailablePlan.getPlan(), selectedTargetCompetence));
		} else if (selectedCompetence != null && selectedAvailablePlan != null) {
			return convertToAppendedLearningPlansData(lpRecommender.getAppendedPlansForCompetence(selectedAvailablePlan.getPlan(), selectedCompetence));
		} else {
			return new ArrayList<AppendedLearningPlanData>();
		}
	}
	private List<AppendedLearningPlanData> convertAppendedActivitiesToAppendedLearningPlansData(List<Activity> appendedActivities) {
		List<AppendedLearningPlanData> appendedPlansData = new ArrayList<AppendedLearningPlanData>();
		//	for (Activity activity : appendedActivities) {
 			 	AppendedLearningPlanData appendedLearningPlanData = new AppendedLearningPlanData();
				appendedLearningPlanData.setActivities(compWallActivityConverter.convertActivities(
						appendedActivities, 
						loggedUser.getUser(),
						loggedUser.getLocale(),
						true, 
						false));
				appendedPlansData.add(appendedLearningPlanData);
		//	}
		//}
		return appendedPlansData;
	}
	/**
	 * @param appendedPlans
	 * @return
	 */
	private List<AppendedLearningPlanData> convertToAppendedLearningPlansData(List<LearningPlan> appendedPlans) {
		List<AppendedLearningPlanData> appendedPlansData = new ArrayList<AppendedLearningPlanData>();
		
		if (appendedPlans != null && !appendedPlans.isEmpty()) {
			for (LearningPlan plan : appendedPlans) {
				System.out.println("Appended plans has:"+plan.getAppendedActivities().size()+" main activities."+plan.getActivities().size());
				AppendedLearningPlanData appendedLearningPlanData = new AppendedLearningPlanData(plan);
				appendedLearningPlanData.setMaker(defaultManager.merge(plan.getMaker()));
				appendedLearningPlanData.setActivities(loadLearningPlanWallActivities(plan));
				appendedPlansData.add(appendedLearningPlanData);
			}
		}
		return appendedPlansData;
	}

	public boolean hasLearningPlanHasActivities(LearningPlan learningPlan) {
	
		boolean value = false;
		if (learningPlan != null) {
			System.out.println("Has learning plan "+learningPlan.getActivities().size()+" app:"+learningPlan.getAppendedActivities().size());
			value = suggLearningQueries.checkIfLearningPlanHasActivities(learningPlan.getMaker(), learningPlan);
		}else{
			System.out.println("Learning plan is null");
		}
		return value;
	}
	public boolean hasPlanDataHasActivities(AppendedLearningPlanData learningPlanData) {
		
		boolean value = false;
		if (learningPlanData != null) {
		if(learningPlanData.getActivities().size()>0);
		value=true;
		}else{
			System.out.println("Learning plan is null");
		}
		return value;
	}

	public List<TargetActivity> loadLearningPlanActivities(LearningPlan learningPlan) {
		if (learningPlan != null) {
			return suggLearningQueries.loadAllActivitiesForLearningPlanByMaker(learningPlan.getMaker(), learningPlan);
		} else
			return null;
	}

	public List<ActivityWallData> loadLearningPlanWallActivities(LearningPlan learningPlan) {
		if (learningPlan != null) {
			List<TargetActivity> activities = loadLearningPlanActivities(learningPlan);

			return compWallActivityConverter.convertToActivityInstances(
					null,
					activities, 
					loggedUser.getUser(), 
					true, 
					false,
					loggedUser.getLocale());
		} else {
			return null;
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public AvailableLearningPlan getSelectedAvailablePlan() {
		return selectedAvailablePlan;
	}
}
