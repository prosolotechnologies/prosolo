package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ExternalActivityResourceData;

public class ExternalActivityMapper extends ActivityMapper {

	public ExternalActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	Activity getActivity() {
		ExternalToolActivity act = new ExternalToolActivity();
		ExternalActivityResourceData res = (ExternalActivityResourceData) activityData.getResourceData();
		act.setAcceptGrades(res.isAcceptGrades());
		act.setConsumerKey(res.getConsumerKey());
		if (act.getConsumerKey() == null || act.getConsumerKey().equals("")) {
			act.setConsumerKey(Settings.getInstance().config.init.bcName);
		}
		act.setSharedSecret(res.getSharedSecret());
		act.setLaunchUrl(res.getLaunchUrl());
		return act;
	}

}
