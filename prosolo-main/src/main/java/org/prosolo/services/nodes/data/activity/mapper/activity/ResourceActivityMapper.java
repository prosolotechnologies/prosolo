package org.prosolo.services.nodes.data.activity.mapper.activity;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.services.nodes.data.activity.ActivityData;
import org.prosolo.services.nodes.data.activity.ResourceActivityResourceData;
import org.prosolo.web.activitywall.data.AttachmentPreview;

public abstract class ResourceActivityMapper extends ActivityMapper {

	public ResourceActivityMapper(ActivityData activityData) {
		super(activityData);
	}

	@Override
	Activity getActivity() {
		ResourceActivity act = new ResourceActivity();
		ResourceActivityResourceData res = (ResourceActivityResourceData) activityData.getResourceData();
		RichContent richC = res.getAttachmentPreview() == null ? null : 
			getRichContent(res.getAttachmentPreview());
		richC.setContentType(getContentType());
		act.setRichContent(richC);
		return act;
	}
	
	private RichContent getRichContent(AttachmentPreview attachmentPreview) {
		RichContent richContent = new RichContent();
		richContent.setTitle(attachmentPreview.getTitle());
		richContent.setDescription(attachmentPreview.getDescription());
		richContent.setImageUrl(attachmentPreview.getImage());
		richContent.setLink(attachmentPreview.getLink());
		return richContent;
	}

	abstract ContentType getContentType();

}
