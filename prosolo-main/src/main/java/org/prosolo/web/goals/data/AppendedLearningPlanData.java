package org.prosolo.web.goals.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.LearningPlan;
import org.prosolo.web.activitywall.data.ActivityWallData;

public class AppendedLearningPlanData implements Serializable {
	
	private static final long serialVersionUID = 7618963848758461110L;

	private User maker;
	private List<ActivityWallData> activities;
	private LearningPlan plan;
	
	public AppendedLearningPlanData() {
		this.activities = new ArrayList<ActivityWallData>();
	}
	
	public AppendedLearningPlanData(LearningPlan plan) {
		this();
		this.plan = plan;
		this.maker = plan.getMaker();
	}
	
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public List<ActivityWallData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityWallData> activities) {
		this.activities = activities;
	}
	
	public boolean addActivitiy(ActivityWallData activity) {
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

}