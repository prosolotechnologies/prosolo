package org.prosolo.services.nodes.data.activity.mapper.activityData;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;
import org.prosolo.services.nodes.data.activity.UploadAssignmentResourceData;

public class AssignmentActivityDataMapper extends ActivityDataMapper {

	public AssignmentActivityDataMapper(Activity activity) {
		super(activity);
	}

	@Override
	ResourceData getResourceData() {
		UploadAssignmentActivity act = (UploadAssignmentActivity) activity;
		UploadAssignmentResourceData res = new UploadAssignmentResourceData();
		res.setDuration(act.getDuration());
		res.setMaxFilesNumber(act.getMaxFilesNumber());
		res.setVisibleToEveryone(act.isVisibleToEveryone());
		
		return res;
	}

	@Override
	ResourceType getActivityType() {
		return ResourceType.ASSIGNMENT;
	}

	@Override
	Class<? extends Activity> getActivityClass() {
		return UploadAssignmentActivity.class;
	}

}
