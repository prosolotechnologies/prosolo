package org.prosolo.services.nodes.data.activity.mapper.activityData;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;

public abstract class ActivityDataMapper {

	protected Activity activity;
	
	public ActivityDataMapper(Activity activity) {
		this.activity = activity;
	}
	
	public ActivityData mapToActivityData() {
		ActivityData activityData = getActivityDataWithCommonData();
		activityData.setActivityType(getActivityType());
		activityData.setResourceData(getResourceData());
		return activityData;
	}
	
	abstract ResourceData getResourceData();

	abstract ResourceType getActivityType();
	
	protected ActivityData getActivityDataWithCommonData() {
		ActivityData data = new ActivityData();
		data.setActivityId(activity.getId());
		data.setTitle(activity.getTitle());
		data.setDescription(activity.getDescription());
		data.setMandatory(activity.isMandatory());
		return data;
	}
}
