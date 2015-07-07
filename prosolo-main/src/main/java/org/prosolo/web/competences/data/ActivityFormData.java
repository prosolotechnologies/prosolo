package org.prosolo.web.competences.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;

/**
 * @author Zoran Jeremic Nov 15, 2013
 */

public class ActivityFormData implements Serializable {
	
	private static final long serialVersionUID = -657944527481849592L;

	// common properties
	private long id;
	private String title;
	private String description;
	private ActivityType type;
	private boolean mandatory;
	
	// Resource type related properties
	private String link;
	private boolean linkInvalid;
	private AttachmentPreview attachmentPreview;
	
	// assignment file upload related properties
	private boolean visibleToEveryone;
	private int duration = 7;// days
	private int maxNumberOfFiles = 1;
	
	//external LTI tool related properties
	private String launchUrl;
	private String sharedSecret;
	private String consumerKey;
	private boolean acceptGrades;


	public ActivityFormData() {
		this.attachmentPreview = new AttachmentPreview();
		this.type = ActivityType.RESOURCE;
	}

	public ActivityFormData(ActivityWallData activityData) {
		this.id = activityData.getObject().getId();
		this.title = activityData.getObject().getTitle();
		this.description = activityData.getObject().getDescription();
		this.attachmentPreview = new AttachmentPreview();
		
		if (activityData.getAttachmentPreview() != null)
			this.attachmentPreview = activityData.getAttachmentPreview();

		if (activityData.getObject().getClazz().equals(UploadAssignmentActivity.class)) {
			this.type = ActivityType.ASSIGNMENTUPLOAD;
			this.visibleToEveryone = activityData.getAttachmentPreview().isVisibleToEveryone();
		} else if (activityData.getObject().getClazz().equals(ResourceActivity.class)) {
			this.type = ActivityType.RESOURCE;
			
			if (activityData.getAttachmentPreview() != null) {
				this.attachmentPreview.setInitialized(true);
				this.link = this.attachmentPreview.getLink();
				this.visibleToEveryone = activityData.getAttachmentPreview().isVisibleToEveryone();
			}
		} else if (activityData.getObject().getClazz().equals(ExternalToolActivity.class)) {
			this.type = ActivityType.EXTERNALTOOL;
			this.launchUrl = activityData.getActivity().getLaunchUrl();
			this.sharedSecret = activityData.getActivity().getSharedSecret();
			this.consumerKey = activityData.getActivity().getConsumerKey();
		}
		
		this.mandatory = activityData.isMandatory();
		
		if (activityData.getAttachmentPreview() != null) {
			this.duration = activityData.getAttachmentPreview().getDuration();
			this.maxNumberOfFiles = activityData.getAttachmentPreview().getMaxFilesNumber();
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

//	public boolean isLinkEnabled() {
//		return linkEnabled;
//	}
//
//	public void setLinkEnabled(boolean linkEnabled) {
//		this.linkEnabled = linkEnabled;
//	}
//
//	public void enableLink() {
//		this.linkEnabled = true;
//	}
//
//	public void disableLink() {
//		this.linkEnabled = false;
//	}
	
	public boolean isLinkInvalid() {
		return linkInvalid;
	}

	public void setLinkInvalid(boolean linkInvalid) {
		this.linkInvalid = linkInvalid;
	}

	public boolean isVisibleToEveryone() {
		return visibleToEveryone;
	}

	public void setVisibleToEveryone(boolean visibleToEveryone) {
		this.visibleToEveryone = visibleToEveryone;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getMaxNumberOfFiles() {
		return maxNumberOfFiles;
	}

	public void setMaxNumberOfFiles(int maxNumberOfFiles) {
		this.maxNumberOfFiles = maxNumberOfFiles;
	}

	public AttachmentPreview getAttachmentPreview() {
		return attachmentPreview;
	}

	public void setAttachmentPreview(AttachmentPreview attachmentPreview) {
		this.attachmentPreview = attachmentPreview;
	}
	
	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}
	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public boolean isAcceptGrades() {
		return acceptGrades;
	}

	public void setAcceptGrades(boolean acceptGrades) {
		this.acceptGrades = acceptGrades;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	
}
