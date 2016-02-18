package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;

public abstract class ResourceActivityMapper extends ActivityMapper {

	public ResourceActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	Activity getActivity() {
		ResourceActivity act = new ResourceActivity();
		ResourceActivityResourceData res = (ResourceActivityResourceData) activityData.getResourceData();
		RichContent richC = res.getRichContent() != null ? res.getRichContent() : new RichContent();
		richC.setContentType(getContentType());
		act.setRichContent(richC);
		return act;
	}
	
	abstract ContentType getContentType();

}
