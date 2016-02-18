package org.prosolo.services.nodes.data.activity.mapper.activity;

import java.util.Date;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.activity.ActivityData;

public abstract class ActivityMapper {

	protected ActivityData activityData;
	protected Activity activity;
	
	public ActivityMapper(ActivityData activityData) {
		this.activityData = activityData;
	}
	
	public ActivityMapper(Activity activity) {
		this.activity = activity;
	}
	
	public Activity mapToActivity() {
		Activity act = getActivity();
		setCommonActivityData(act);
		return act;
	}

	abstract Activity getActivity();
	
	//this method changes state of the object passed
	protected void setCommonActivityData(Activity activity) {
		activity.setTitle(activityData.getTitle());
		activity.setDescription(activityData.getDescription());
		activity.setMandatory(activityData.isMandatory());
		User maker = new User();
		maker.setId(activityData.getMakerId());
		activity.setMaker(maker);
		activity.setDateCreated(new Date());
		
		if (activityData.getVisibilityType() != null) {
			activity.setVisibility(activityData.getVisibilityType());
		} else {
			activity.setVisibility(VisibilityType.PRIVATE);
		}
	}
	
}
