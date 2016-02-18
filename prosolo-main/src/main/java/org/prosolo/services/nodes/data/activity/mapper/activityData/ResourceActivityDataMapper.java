package org.prosolo.services.nodes.data.activity.mapper.activityData;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;
import org.prosolo.services.nodes.data.activity.ResourceData;
import org.prosolo.services.nodes.data.activity.ResourceType;

public class ResourceActivityDataMapper extends ActivityDataMapper {

	public ResourceActivityDataMapper(Activity activity) {
		super(activity);
	}

	@Override
	ResourceData getResourceData() {
		ResourceActivity act = (ResourceActivity) activity;
		ResourceActivityResourceData res = new ResourceActivityResourceData();
		res.setRichContent(act.getRichContent());
		
		return res;
	}

	@Override
	ResourceType getActivityType() {
		ResourceActivity act = (ResourceActivity) activity;
		ResourceType type = null;
		RichContent richContent = act.getRichContent();
		if(richContent != null) {
			ContentType contentType = richContent.getContentType();
			if(contentType == null) {
				return ResourceType.NONE;
			}
			switch(contentType) {
				case EXTERNALTOOL:
					type = ResourceType.EXTERNAL_ACTIVITY;
					break;
				case NONE:
					type = ResourceType.NONE;
					break;
				case LINK:
					type = ResourceType.URL;
					break;
				case RESOURCE:
					type = ResourceType.FILE;
					break;
				case SLIDES:
					type = ResourceType.SLIDESHARE;
					break;
				case UPLOAD:
					type = ResourceType.FILE;
					break;
				case UPLOAD_ASSIGNMENT:
					type = ResourceType.ASSIGNMENT;
					break;
				case VIDEO:
					type = ResourceType.VIDEO;
					break;
			}
			return type;
		} else {
			return ResourceType.NONE;
		}
	}

}
