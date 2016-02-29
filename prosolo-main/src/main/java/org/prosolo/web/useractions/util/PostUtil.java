/**
 * 
 */
package org.prosolo.web.useractions.util;

import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview;
import org.prosolo.web.useractions.data.NewPostData;

/**
 * @author "Nikola Milikic"
 *
 */
public class PostUtil {

	public static NewPostData convertSocialActivityToNewPostData(SocialActivity socialActivity) {
		NewPostData newPostData = new NewPostData();
		newPostData.setText(socialActivity.getText());
		newPostData.setAttachmentPreview(convertRichContentToAttachmentPreview(socialActivity.getRichContent()));
		
		return newPostData;
	}

	private static AttachmentPreview convertRichContentToAttachmentPreview(RichContent richContent) {
		if (richContent != null) {
			
			AttachmentPreview attachmentPreview = new AttachmentPreview();
			attachmentPreview.setContentType(richContent.getContentType());
			attachmentPreview.setLink(richContent.getLink());
			attachmentPreview.setInitialized(true);

			if (richContent.getContentType().equals(ContentType.LINK)) {
				attachmentPreview.setTitle(richContent.getTitle());
				attachmentPreview.setDescription(richContent.getDescription());
			} else if (richContent.getContentType().equals(ContentType.UPLOAD)) {
				attachmentPreview.setUploadTitle(richContent.getTitle());
				attachmentPreview.setUploadDescription(richContent.getDescription());
			}
			
			String selectedImage = richContent.getImageUrl();
			attachmentPreview.setImage(selectedImage);
			attachmentPreview.getImages().add(selectedImage);
			attachmentPreview.setSelectedImageIndex(0);
			
			return attachmentPreview;
		}
		return new AttachmentPreview();
	}
	
}
