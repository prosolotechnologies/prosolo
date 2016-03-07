package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.UploadAssignmentResourceData;

public class AssignmentActivityMapper extends ActivityMapper {

	public AssignmentActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	Activity getActivity() {
		UploadAssignmentActivity act = new UploadAssignmentActivity();
		UploadAssignmentResourceData res = (UploadAssignmentResourceData) activityData.getResourceData();
		act.setDuration(res.getDuration());
		act.setMaxFilesNumber(res.getMaxFilesNumber());
		act.setVisibleToEveryone(res.isVisibleToEveryone());
		return act;
	}

}
