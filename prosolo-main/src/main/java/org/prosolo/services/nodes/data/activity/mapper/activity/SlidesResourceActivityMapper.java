package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.services.nodes.data.activity.ActivityData;

public class SlidesResourceActivityMapper extends ResourceActivityMapper {

	public SlidesResourceActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	ContentType getContentType() {
		return ContentType.SLIDES;
	}

}
