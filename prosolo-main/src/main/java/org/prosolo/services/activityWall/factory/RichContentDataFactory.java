package org.prosolo.services.activityWall.factory;

import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.statusWall.*;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.util.url.URLUtil;
import org.springframework.stereotype.Component;

@Component
public class RichContentDataFactory {

    public AttachmentPreview getAttachmentPreview(RichContent1 richContent) throws LinkParserException {
        if (richContent == null) {
            return null;
        }
        AttachmentPreview attachPreview = new AttachmentPreview();
        attachPreview.setTitle(richContent.getTitle());
        attachPreview.setDescription(richContent.getDescription());
        attachPreview.setLink(richContent.getLink());
        attachPreview.setEmbedId(richContent.getEmbedId());
        attachPreview.setContentType(richContent.getContentType());
        if (attachPreview.getContentType() == ContentType1.LINK) {
            attachPreview.setDomain(URLUtil.getDomainFromUrl(attachPreview.getLink()));
        }
        if (attachPreview.getContentType() == ContentType1.FILE) {
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

    public AttachmentPreview getAttachmentPreviewForCredential(long id, long duration,
                                                               String title, String description, LearningResourceType type, String creatorName,
                                                               String creatorLastname) {
        CredentialPostData ap = new CredentialPostData();
        ap.setMediaType(MediaType1.Credential);
        ap.setCredentialId(id);
        ap.setDuration(TimeUtil.getHoursAndMinutesInString(duration));
        ap.setTitle(title);
        ap.setDescription(description);
        ap.setUniversityCreated(type == LearningResourceType.UNIVERSITY_CREATED);
        ap.setCreatorName(getFullName(creatorName, creatorLastname));
        ap.setInitialized(true);
        return ap;
    }

    public AttachmentPreview getAttachmentPreviewForCompetence(long id, long duration,
                                                               String title, String description, LearningResourceType type,
                                                               String creatorName, String creatorLastname, long credId) {
        CompetencePostData ap = new CompetencePostData();
        ap.setMediaType(MediaType1.Competence);
        ap.setCredentialId(credId);
        ap.setCompetenceId(id);
        ap.setDuration(TimeUtil.getHoursAndMinutesInString(duration));
        ap.setTitle(title);
        ap.setDescription(description);
        ap.setUniversityCreated(type == LearningResourceType.UNIVERSITY_CREATED);
        ap.setCreatorName(getFullName(creatorName, creatorLastname));
        ap.setInitialized(true);

        return ap;
    }

    public AttachmentPreview getAttachmentPreviewForActivity(long id, long duration,
                                                             String title, String description, LearningResourceType type, ActivityType activityType,
                                                             String creatorName, String creatorLastname, long compId, long credId) {
        ActivityPostData ap = new ActivityPostData();
        ap.setMediaType(MediaType1.Activity);
        ap.setCredentialId(credId);
        ap.setCompetenceId(compId);
        ap.setActivityId(id);
        ap.setDuration(TimeUtil.getHoursAndMinutesInString(duration));
        ap.setTitle(title);
        ap.setDescription(description);
        ap.setUniversityCreated(type == LearningResourceType.UNIVERSITY_CREATED);
        ap.setCreatorName(getFullName(creatorName, creatorLastname));
        ap.setInitialized(true);
        ap.setActivityType(activityType);
        return ap;
    }

    /**
     * Returns attachment preview for comment
     *
     * @param commentId comment id
     * @param type      object type for which comment is created
     * @param title
     * @param comment   comment text
     * @param compId    comeptence id
     * @param actId     activity id
     * @return
     */
    public AttachmentPreview getAttachmentPreviewForComment(long commentId,
                                                            ResourceType type, String title, String comment, long compId, long actId, ActivityType actType) {
        CommentPostData ap = new CommentPostData();
        MediaType1 mediaType;

        if (type == ResourceType.Competence) {
            mediaType = MediaType1.CompetenceComment;
        } else {
            mediaType = MediaType1.ActivityComment;
            ap.setActivityType(actType);
        }

        // TODO: PRS-3638
        int credId = 1;

        ap.setId(commentId);
        ap.setCredentialId(credId);
        ap.setCompetenceId(compId);
        ap.setActivityId(actId);
        ap.setTitle(title);
        ap.setDescription(comment);
        ap.setMediaType(mediaType);
        ap.setInitialized(true);
        return ap;
    }

    public AttachmentPreview getAttachmentPreviewForTwitterPost(String nick, String profileUrl,
                                                                String text, String postUrl) {
        TwitterPostData ap = new TwitterPostData();
        ap.setMediaType(MediaType1.Tweet);
        ap.setNickname(nick);
        ap.setProfileUrl(profileUrl);
        ap.setDescription(text);
        ap.setLink(postUrl);
        ap.setInitialized(true);
        return ap;
    }

    public MediaData getMediaData(AttachmentPreview attachPreview) throws LinkParserException {
        MediaType1 mediaType = MediaType1.Link_Other;
        String embedLink = null;
        String embedId = null;

        if (attachPreview.getContentType() == ContentType1.LINK) {
            String link = attachPreview.getLink();
            if (URLUtil.checkIfSlidesharePresentationLink(link)) {
                MediaData mediaData = URLUtil.getSlideshareEmbedLink(link, attachPreview.getEmbedId());
                if (mediaData != null) {
                    return mediaData;
                }
            } else if (URLUtil.checkIfYoutubeLink(link)) {
                mediaType = MediaType1.Youtube;
                embedId = URLUtil.getYoutubeEmbedId(link);
            }
        } else {
            mediaType = MediaType1.File_Other;
        }
        return new MediaData(mediaType, embedLink, embedId);
    }

    private String getFullName(String name, String lastName) {
        return name + (lastName != null ? " " + lastName : "");
    }

    public RichContent1 getRichContent(AttachmentPreview attachmentPreview) {
        if (attachmentPreview == null) {
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
