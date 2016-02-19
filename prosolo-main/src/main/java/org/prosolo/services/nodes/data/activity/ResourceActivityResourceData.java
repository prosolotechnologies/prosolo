package org.prosolo.services.nodes.data.activity;

import org.prosolo.web.activitywall.data.AttachmentPreview;

public class ResourceActivityResourceData extends ResourceData {

	private AttachmentPreview attachmentPreview;
	
	public ResourceActivityResourceData() {
		setActivityType();
	}

	public ResourceActivityResourceData(AttachmentPreview attachment) {
		setActivityType();
	}
	
	@Override
	void setActivityType() {
		this.activityType = ActivityType.RESOURCE;
	}

	public AttachmentPreview getAttachmentPreview() {
		return attachmentPreview;
	}

	public void setAttachmentPreview(AttachmentPreview attachmentPreview) {
		this.attachmentPreview = attachmentPreview;
	}
	
}
