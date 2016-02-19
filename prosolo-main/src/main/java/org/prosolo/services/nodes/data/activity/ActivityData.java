package org.prosolo.services.nodes.data.activity;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.organization.VisibilityType;

public class ActivityData implements Serializable{

	private static final long serialVersionUID = -1506416320540794846L;
	
	//competence activity data
	private long competenceActivityId;
	private long order;
	//activity data
	private long activityId;
	private String title;
	private String description;
	private boolean mandatory;
	private long makerId;
	private VisibilityType visibilityType;
	private ResourceType resourceType;
	private ResourceData resourceData;
	
	private Class<? extends Activity> activityClass;
	
	public ActivityData() {
		this.resourceType = ResourceType.NONE;
		this.visibilityType = VisibilityType.PUBLIC;
	}

	public ActivityData(Activity activity) { 
		this.activityId = activity.getId();
		this.title = activity.getTitle();
		this.description = activity.getDescription();
		this.mandatory = activity.isMandatory();
	}
	
	public static ActivityData copyActivity(ActivityData data) {
		ActivityData ad = new ActivityData();
		
		ad.setCompetenceActivityId(data.getCompetenceActivityId());
		ad.setOrder(data.getOrder());
		ad.setActivityId(data.getActivityId());
		ad.setTitle(data.getTitle());
		ad.setDescription(data.getDescription());
		ad.setMandatory(data.isMandatory());
		
		return ad;
	}
	
	public void createResourceDataBasedOnResourceType() {
		switch(resourceType) {
			case ASSIGNMENT:
				this.resourceData = new UploadAssignmentResourceData();
				return;
			case EXTERNAL_ACTIVITY:
				this.resourceData = new ExternalActivityResourceData();
				return;
			case FILE:
			case SLIDESHARE:
			case URL:
			case VIDEO:
				this.resourceData = new ResourceActivityResourceData();
				return;
			case NONE:
				this.resourceData = null;
				return;
		}
	}
	
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public long getCompetenceActivityId() {
		return competenceActivityId;
	}

	public void setCompetenceActivityId(long competenceActivityId) {
		this.competenceActivityId = competenceActivityId;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType activityType) {
		this.resourceType = activityType;
	}

	public ResourceData getResourceData() {
		return resourceData;
	}

	public void setResourceData(ResourceData resourceData) {
		this.resourceData = resourceData;
	}

	public long getMakerId() {
		return makerId;
	}

	public void setMakerId(long makerId) {
		this.makerId = makerId;
	}

	public VisibilityType getVisibilityType() {
		return visibilityType;
	}

	public void setVisibilityType(VisibilityType visibilityType) {
		this.visibilityType = visibilityType;
	}
	
	public Class<? extends Activity> getActivityClass () {
		return activityClass;
	}

	public void setActivityClass(Class<? extends Activity> activityClass) {
		this.activityClass = activityClass;
	}
	
}
