package org.prosolo.services.nodes.data.activity;

public abstract class ResourceData {

	protected ActivityType activityType;

	public ActivityType getActivityType() {
		return activityType;
	}

	abstract void setActivityType();
	
}
