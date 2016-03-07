package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.services.nodes.data.activity.ActivityData;

public class NoneResourceActivityMapper extends ResourceActivityMapper {

	public NoneResourceActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	ContentType getContentType() {
		return ContentType.NONE;
	}

}
