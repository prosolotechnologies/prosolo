package org.prosolo.web.goals.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.workflow.LearningPlan;

public class AvailableLearningPlan implements Serializable {
	
	private static final long serialVersionUID = 8508197094165582552L;

	private int number;
	private List<RecommendedActivityData> activities;
	private LearningPlan plan;
	private boolean allActivitiesInCurrentPlan;
	
	public AvailableLearningPlan() {
		this.activities = new ArrayList<RecommendedActivityData>();
	}
	
	public AvailableLearningPlan(LearningPlan plan) {
		this();
		this.plan = plan;
	}
	
	public AvailableLearningPlan(int number, LearningPlan plan) {
		this(plan);
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public List<RecommendedActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<RecommendedActivityData> activities) {
		this.activities = activities;
	}
	
	public boolean addActivitiy(RecommendedActivityData activity) {
		if (activity != null) {
			return getActivities().add(activity);
		}
		return false;
	}
	
	public LearningPlan getPlan() {
		return plan;
	}

	public void setPlan(LearningPlan plan) {
		this.plan = plan;
	}

	public boolean isAllActivitiesInCurrentPlan() {
		return allActivitiesInCurrentPlan;
	}

	public void setAllActivitiesInCurrentPlan(boolean allActivitiesInCurrentPlan) {
		this.allActivitiesInCurrentPlan = allActivitiesInCurrentPlan;
	}
	
}