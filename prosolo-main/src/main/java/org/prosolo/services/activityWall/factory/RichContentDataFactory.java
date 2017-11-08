package org.prosolo.services.activityWall.factory;

import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaType1;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.page.ObjectToPageMapper;
import org.prosolo.services.util.url.URLUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class RichContentDataFactory {

	@Inject private UrlIdEncoder idEncoder;

	public AttachmentPreview1 getAttachmentPreview(RichContent1 richContent) throws LinkParserException {
		if(richContent == null) {
			return null;
		}
		AttachmentPreview1 attachPreview = new AttachmentPreview1();
		attachPreview.setTitle(richContent.getTitle());
		attachPreview.setDescription(richContent.getDescription());
		attachPreview.setLink(richContent.getLink());
		attachPreview.setEmbedId(richContent.getEmbedId());
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
		attachPreview.setEmbedId(md.getEmbedId());
		attachPreview.setInitialized(true);

		return attachPreview;
	}

	public AttachmentPreview1 getAttachmentPreviewForCredential(long id, long duration,
			String title, String description, LearningResourceType type, String creatorName,
			String creatorLastname) {
		AttachmentPreview1 ap = getAttachmentPreviewForLearningResource(id, duration, title, description,
				type, creatorName, creatorLastname, MediaType1.Credential);
		String page = ObjectToPageMapper.getViewPageForObjectType(ResourceType.Credential);
		ap.setLink(page + "?id=" + idEncoder.encodeId(id));
		return ap;
	}

	public AttachmentPreview1 getAttachmentPreviewForCompetence(long id, long duration,
			String title, String description, LearningResourceType type,
			String creatorName, String creatorLastname, long credId) {
		AttachmentPreview1 ap = getAttachmentPreviewForLearningResource(id, duration, title, description,
				type, creatorName, creatorLastname, MediaType1.Competence);
		String page = ObjectToPageMapper.getViewPageForObjectType(ResourceType.Competence);
		StringBuilder url = new StringBuilder(page);
		url.append("?compId=" + idEncoder.encodeId(id));
		if(credId > 0) {
			url.append("&credId=" + idEncoder.encodeId(credId));
		}
		ap.setLink(url.toString());
		return ap;
	}

	public AttachmentPreview1 getAttachmentPreviewForActivity(long id, long duration,
			String title, String description, LearningResourceType type, ActivityType activityType,
			String creatorName, String creatorLastname, long compId, long credId) {
		AttachmentPreview1 ap = getAttachmentPreviewForLearningResource(id, duration, title, description,
				type, creatorName, creatorLastname, MediaType1.Activity);
		ap.setActivityType(activityType);
		String page = ObjectToPageMapper.getViewPageForObjectType(ResourceType.Activity);
		StringBuilder url = new StringBuilder(page);
		url.append("?actId=" + idEncoder.encodeId(id));
		if(compId > 0) {
			url.append("&compId=" + idEncoder.encodeId(compId));
		}
		if(credId > 0) {
			url.append("&credId=" + idEncoder.encodeId(credId));
		}
		ap.setLink(url.toString());
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

	/**
	 * Returns attachment preview for comment
	 * @param id
	 * @param type object type for which comment is created
	 * @param title
	 * @param comment
	 * @param compId
	 * @param actId
	 * @return
	 */
	public AttachmentPreview1 getAttachmentPreviewForComment(long id,
			ResourceType type, String title, String comment, long compId, long actId, ActivityType actType) {
		AttachmentPreview1 ap = new AttachmentPreview1();
		MediaType1 mediaType = null;
		if(type == ResourceType.Competence) {
			mediaType = MediaType1.CompetenceComment;
		} else {
			mediaType = MediaType1.ActivityComment;
			ap.setActivityType(actType);
		}
		ap.setMediaType(mediaType);
		ap.setId(id);
		ap.setTitle(title);
		ap.setDescription(comment);
		String page = ObjectToPageMapper.getViewPageForObjectType(type);
		StringBuilder url = new StringBuilder(page);
		url.append("?comment=" + idEncoder.encodeId(id));
		if(compId > 0) {
			url.append("&compId=" + idEncoder.encodeId(compId));
		}
		if(actId > 0) {
			url.append("&actId=" + idEncoder.encodeId(actId));
		}
		ap.setLink(url.toString());
		ap.setInitialized(true);
		return ap;
	}

	public AttachmentPreview1 getAttachmentPreviewForTwitterPost(String nick, String profileUrl,
			String text, String postUrl) {
		AttachmentPreview1 ap = new AttachmentPreview1();
		ap.setMediaType(MediaType1.Tweet);
		ap.setNickname(nick);
		ap.setProfileUrl(profileUrl);
		ap.setDescription(text);
		ap.setLink(postUrl);
		ap.setInitialized(true);
		return ap;
	}

	public MediaData getMediaData(AttachmentPreview1 attachPreview) throws LinkParserException {
		MediaType1 mediaType = MediaType1.Link_Other;
		String embedLink = null;
		String embedId = null;
		if(attachPreview.getContentType() == ContentType1.LINK) {
			String link = attachPreview.getLink();
			if(URLUtil.checkIfSlidesharePresentationLink(link)) {
				MediaData mediaData = URLUtil.getSlideshareEmbedLink(link, attachPreview.getEmbedId());
                if (mediaData != null) {
                    return mediaData;
                }
            } else if(URLUtil.checkIfYoutubeLink(link)) {
				mediaType = MediaType1.Youtube;
				embedId = URLUtil.getYoutubeEmbedId(link);
				//return URLUtil.getYoutubeMediaData(link);
			}
		} else {
			mediaType = MediaType1.File_Other;
		}
		//TODO if it is needed to differentiate file types do that here before setting media type
		return new MediaData(mediaType, embedLink, embedId);
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
		richContent.setEmbedId(attachmentPreview.getEmbedId());
		richContent.setContentType(attachmentPreview.getContentType());
		return richContent;
	}

}
