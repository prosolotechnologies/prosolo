package org.prosolo.services.nodes.data.activity.mapper;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ResourceType;
import org.prosolo.services.nodes.data.activity.mapper.activity.ActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.AssignmentActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.ExternalActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.FileResourceActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.NoneResourceActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.SlidesResourceActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.UrlResourceActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activity.VideoResourceActivityMapper;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ActivityDataMapper;
import org.prosolo.services.nodes.data.activity.mapper.activityData.AssignmentActivityDataMapper;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ExternalActivityDataMapper;
import org.prosolo.services.nodes.data.activity.mapper.activityData.ResourceActivityDataMapper;

public class ActivityMapperFactory {

	public static ActivityMapper getActivityMapper(ActivityData activityData) {
		ActivityMapper activityMapper = null;
		ResourceType type = activityData.getActivityType();
		if(type == null) {
			return null;
		}
		switch(type) {
			case NONE:
				activityMapper = new NoneResourceActivityMapper(activityData);
				break;
			case FILE:
				activityMapper = new FileResourceActivityMapper(activityData);
				break;
			case URL:
				activityMapper = new UrlResourceActivityMapper(activityData);
				break;
			case SLIDESHARE:
				activityMapper = new SlidesResourceActivityMapper(activityData);
				break;
			case VIDEO:
				activityMapper = new VideoResourceActivityMapper(activityData);
				break;
			case ASSIGNMENT:
				activityMapper = new AssignmentActivityMapper(activityData);
				break;
			case EXTERNAL_ACTIVITY:
				activityMapper = new ExternalActivityMapper(activityData);
				break;
		}
		return activityMapper;
	}
	
	public static ActivityDataMapper getActivityDataMapper(Activity activity) {
		ActivityDataMapper activityDataMapper = null;
		if(activity instanceof ExternalToolActivity) {
			activityDataMapper = new ExternalActivityDataMapper(activity);
		} else if(activity instanceof UploadAssignmentActivity) {
			activityDataMapper = new AssignmentActivityDataMapper(activity);
		} else if(activity instanceof ResourceActivity) {
			activityDataMapper = new ResourceActivityDataMapper(activity);
		}
		
		return activityDataMapper;
	}
	
}
