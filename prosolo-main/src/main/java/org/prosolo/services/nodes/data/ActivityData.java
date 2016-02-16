package org.prosolo.services.nodes.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.Activity;

public class ActivityData implements Serializable{

	private static final long serialVersionUID = -1506416320540794846L;
	
	//competence activity data
	private long competenceActivityId;
	private long order;
	//activity data
	private long activityId;
	private String title;
	private String description;
	private boolean mandatory;
	
	public ActivityData() {
	
	}

	public ActivityData(Activity activity) { 
		this.activityId = activity.getId();
		this.title = activity.getTitle();
		this.description = activity.getDescription();
		this.mandatory = activity.isMandatory();
	}
	
	public static ActivityData copyActivity(ActivityData data) {
		ActivityData ad = new ActivityData();
		
		ad.setCompetenceActivityId(data.getCompetenceActivityId());
		ad.setOrder(data.getOrder());
		ad.setActivityId(data.getActivityId());
		ad.setTitle(data.getTitle());
		ad.setDescription(data.getDescription());
		ad.setMandatory(data.isMandatory());
		
		return ad;
	}
	
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public long getCompetenceActivityId() {
		return competenceActivityId;
	}

	public void setCompetenceActivityId(long competenceActivityId) {
		this.competenceActivityId = competenceActivityId;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}
	
}
