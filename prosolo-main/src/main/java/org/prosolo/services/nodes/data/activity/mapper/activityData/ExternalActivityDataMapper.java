package org.prosolo.services.nodes.data.activity.mapper.activityData;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.services.nodes.data.activity.ExternalActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;

public class ExternalActivityDataMapper extends ActivityDataMapper {

	public ExternalActivityDataMapper(Activity activity) {
		super(activity);
	}

	@Override
	ResourceData getResourceData() {
		ExternalToolActivity act = (ExternalToolActivity) activity;
		ExternalActivityResourceData res = new ExternalActivityResourceData();
		res.setConsumerKey(act.getConsumerKey());
		res.setLaunchUrl(act.getLaunchUrl());
		res.setSharedSecret(act.getSharedSecret());
		res.setAcceptGrades(act.isAcceptGrades());
		
		return res;
	}

	@Override
	ResourceType getActivityType() {
		return ResourceType.EXTERNAL_ACTIVITY;
	}

}
