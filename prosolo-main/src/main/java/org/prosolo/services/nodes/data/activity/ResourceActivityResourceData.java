package org.prosolo.services.nodes.data.activity;

import org.prosolo.common.domainmodel.content.RichContent;

public class ResourceActivityResourceData extends ResourceData {

	private RichContent richContent;
	
	public ResourceActivityResourceData() {
		setActivityType();
	}

	public ResourceActivityResourceData(RichContent richContent) {
		this.richContent = richContent;
		setActivityType();
	}


	public RichContent getRichContent() {
		return richContent;
	}

	public void setRichContent(RichContent richContent) {
		this.richContent = richContent;
	}

	@Override
	void setActivityType() {
		this.activityType = ActivityType.RESOURCE;
	}
	
	
}
