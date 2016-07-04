package org.prosolo.services.activityWall.factory;

import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaType1;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.util.url.URLUtil;
import org.springframework.stereotype.Component;

@Component
public class RichContentDataFactory {

	public AttachmentPreview1 getAttachmentPreview(RichContent1 richContent) {
		if(richContent == null) {
			return null;
		}
		AttachmentPreview1 attachPreview = new AttachmentPreview1();
		attachPreview.setTitle(richContent.getTitle());
		attachPreview.setDescription(richContent.getDescription());
		attachPreview.setLink(richContent.getLink());
		attachPreview.setContentType(richContent.getContentType());
		if(attachPreview.getContentType() == ContentType1.LINK) {
			attachPreview.setDomain(URLUtil.getDomainFromUrl(attachPreview.getLink()));
		}
		if(attachPreview.getContentType() == ContentType1.FILE) {
			attachPreview.setFileName(attachPreview.getLink().substring(
					attachPreview.getLink().lastIndexOf("/") + 1));
		}
		String imageUrl = richContent.getImageUrl();
		if (imageUrl != null) {
			attachPreview.setImageUrl(imageUrl);
			attachPreview.setImageSize(richContent.getImageSize());
//			attachPreview.getImages().add(imageUrl);
//			attachPreview.setSelectedImageIndex(0);
		}
		
		MediaData md = getMediaData(attachPreview);
		attachPreview.setMediaType(md.getMediaType());
		attachPreview.setEmbedingLink(md.getEmbedLink());
		attachPreview.setInitialized(true);
		
		return attachPreview;
	}
	
	public AttachmentPreview1 getAttachmentPreviewForCredential(long id, long duration,
			String title, String description, LearningResourceType type, 
			String creatorName, String creatorLastname) {
		return getAttachmentPreviewForLearningResource(id, duration, title, description, 
				type, creatorName, creatorLastname, MediaType1.Credential);
	}
	
	public AttachmentPreview1 getAttachmentPreviewForCompetence(long id, long duration,
			String title, String description, LearningResourceType type, 
			String creatorName, String creatorLastname) {
		return getAttachmentPreviewForLearningResource(id, duration, title, description, 
				type, creatorName, creatorLastname, MediaType1.Competence);
	}
	
	public AttachmentPreview1 getAttachmentPreviewForActivity(long id, long duration,
			String title, String description, LearningResourceType type, ActivityType activityType,
			String creatorName, String creatorLastname, long compId, long credId) {
		AttachmentPreview1 ap = getAttachmentPreviewForLearningResource(id, duration, title, description, 
				type, creatorName, creatorLastname, MediaType1.Activity);
		ap.setActivityType(activityType);
		ap.setCompId(compId);
		ap.setCredId(credId);
		return ap;
	}
	
	public AttachmentPreview1 getAttachmentPreviewForLearningResource(long id, long duration,
			String title, String description, LearningResourceType type, 
			String creatorName, String creatorLastname, MediaType1 mediaType) {
		AttachmentPreview1 ap = new AttachmentPreview1();
		ap.setMediaType(mediaType);
		ap.setId(id);
		ap.setDuration(TimeUtil.getHoursAndMinutesInString(duration));
		ap.setTitle(title);
		ap.setDescription(description);
		ap.setUniversityCreated(type == LearningResourceType.UNIVERSITY_CREATED);
		ap.setCreatorName(getFullName(creatorName, creatorLastname));
		ap.setInitialized(true);
		return ap;
	}
	
	public MediaData getMediaData(AttachmentPreview1 attachPreview) {
		MediaType1 mediaType = MediaType1.Link_Other;
		String embedLink = null;
		if(attachPreview.getContentType() == ContentType1.LINK) {
			String link = attachPreview.getLink();
			if(URLUtil.checkIfSlideshareLink(link)) {
				mediaType = MediaType1.Slideshare;
				embedLink = URLUtil.getSlideshareEmbedLink(link);
			} else if(URLUtil.checkIfYoutubeLink(link)) {
				mediaType = MediaType1.Youtube;
				embedLink = URLUtil.getYoutubeEmbedLink(link);
			}
		} else {
			mediaType = MediaType1.File_Other;
		}
		//TODO if it is needed to differentiate file types do that here before setting media type
		return new MediaData(mediaType, embedLink);
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}

	public RichContent1 getRichContent(AttachmentPreview1 attachmentPreview) {
		if(attachmentPreview == null) {
			return null;
		}
		RichContent1 richContent = new RichContent1();
		richContent.setTitle(attachmentPreview.getTitle());
		richContent.setDescription(attachmentPreview.getDescription());
		richContent.setImageUrl(attachmentPreview.getImageUrl());
		richContent.setImageSize(attachmentPreview.getImageSize());
		richContent.setLink(attachmentPreview.getLink());
		richContent.setContentType(attachmentPreview.getContentType());
		return richContent;
	}

}
