package org.prosolo.services.nodes.data.activity;

import java.util.Date;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.User;

@Deprecated
public class ActivityTypeMapper {

	public static ActivityData mapToActivityData(Activity activity) {
		ResourceType actType = null;
		ResourceData resData = null;
		
		if(activity instanceof ExternalToolActivity) {
			actType = ResourceType.EXTERNAL_ACTIVITY;
			resData = createResourceDataForExternalActivity((ExternalToolActivity) activity);
		} else if(activity instanceof UploadAssignmentActivity) {
			actType = ResourceType.ASSIGNMENT;
			resData = createResourceDataForAssignmentActivity((UploadAssignmentActivity) activity);
		} else if(activity instanceof ResourceActivity) {
			actType = getActivityTypeForResourceActivity((ResourceActivity) activity);
			resData = createResourceDataForResourceActivity((ResourceActivity) activity);
		}
		
		ActivityData activityData = new ActivityData();
		activityData.setActivityType(actType);
		activityData.setResourceData(resData);
		return activityData;
	}
	
	public static Activity mapToActivity(ActivityData activityData) {
		ResourceType type = activityData.getActivityType();
		if(type == null) {
			return null;
		}
		Activity activity = null;
		switch(type) {
			case NONE:
				activity = createResourceActivity(activityData, ContentType.NONE);
			    break;
			case FILE:
				activity = createResourceActivity(activityData, ContentType.UPLOAD);
				break;
			case URL:
				activity = createResourceActivity(activityData, ContentType.LINK);
				break;
			case SLIDESHARE:
				activity = createResourceActivity(activityData, ContentType.SLIDES);
				break;
			case VIDEO:
				activity = createResourceActivity(activityData, ContentType.VIDEO);
				break;
			case ASSIGNMENT:
				activity = createUploadAssignmentActivity(activityData);
				break;
			case EXTERNAL_ACTIVITY:
				activity = createExternalToolActivity(activityData);
				break;
		}
		return activity;
	}
	
	private static ResourceData createResourceDataForAssignmentActivity(UploadAssignmentActivity activity) {
		UploadAssignmentResourceData res = new UploadAssignmentResourceData();
		res.setDuration(activity.getDuration());
		res.setMaxFilesNumber(activity.getMaxFilesNumber());
		res.setVisibleToEveryone(activity.isVisibleToEveryone());
		
		return res;
	}
	
	private static ResourceData createResourceDataForResourceActivity(ResourceActivity activity) {
		ResourceActivityResourceData res = new ResourceActivityResourceData();
		res.setRichContent(activity.getRichContent());
		
		return res;
	}

	private static ResourceData createResourceDataForExternalActivity(ExternalToolActivity act) {
		ExternalActivityResourceData res = new ExternalActivityResourceData();
		res.setConsumerKey(act.getConsumerKey());
		res.setLaunchUrl(act.getLaunchUrl());
		res.setSharedSecret(act.getSharedSecret());
		res.setAcceptGrades(act.isAcceptGrades());
		
		return res;
	}

	private static ResourceActivity createResourceActivity(ActivityData data, ContentType cType) {
		ResourceActivity act = new ResourceActivity();
		setCommonActivityData(act, data);
		ResourceActivityResourceData res = (ResourceActivityResourceData) data.getResourceData();
		RichContent richC = res.getRichContent() != null ? res.getRichContent() : new RichContent(); 
		richC.setContentType(cType);
		act.setRichContent(richC);
		return act;
	}
	
	//this method changes state of the object passed
	private static void setCommonActivityData(Activity activity, ActivityData data) {
		activity.setTitle(data.getTitle());
		activity.setDescription(data.getDescription());
		activity.setMandatory(data.isMandatory());
		User maker = new User();
		maker.setId(data.getMakerId());
		activity.setMaker(maker);
		activity.setDateCreated(new Date());
		
		if (data.getVisibilityType() != null) {
			activity.setVisibility(data.getVisibilityType());
		} else {
			activity.setVisibility(VisibilityType.PRIVATE);
		}
	}

	private static ExternalToolActivity createExternalToolActivity(ActivityData data) {
		ExternalToolActivity act = new ExternalToolActivity();
		setCommonActivityData(act, data);
		ExternalActivityResourceData res = (ExternalActivityResourceData) data.getResourceData();
		act.setAcceptGrades(res.isAcceptGrades());
		act.setConsumerKey(res.getConsumerKey());
		if (act.getConsumerKey() == null || act.getConsumerKey().equals("")) {
			act.setConsumerKey(Settings.getInstance().config.init.bcName);
		}
		act.setSharedSecret(res.getSharedSecret());
		act.setLaunchUrl(res.getLaunchUrl());
		return act;
	}
	
	private static UploadAssignmentActivity createUploadAssignmentActivity(ActivityData data) {
		UploadAssignmentActivity act = new UploadAssignmentActivity();
		setCommonActivityData(act, data);
		UploadAssignmentResourceData res = (UploadAssignmentResourceData) data.getResourceData();
		act.setDuration(res.getDuration());
		act.setMaxFilesNumber(res.getMaxFilesNumber());
		act.setVisibleToEveryone(res.isVisibleToEveryone());
		return act;
	}

	private static ResourceType getActivityTypeForResourceActivity(ResourceActivity activity) {
		ResourceType type = null;
		RichContent richContent = activity.getRichContent();
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
