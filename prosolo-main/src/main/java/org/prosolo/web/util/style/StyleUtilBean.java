package org.prosolo.web.util.style;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.CredentialDeliveryStatus;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaType1;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "styleUtilBean")
@Component("styleUtilBean")
@Scope("request")
public class StyleUtilBean implements Serializable {

	private static final long serialVersionUID = 3275340449093388469L;

	public String getStyleClassBasedOnActivityType(ActivityType type) {
		if (type == null) {
			return "";
		}
		switch(type) {
			case TEXT:
				return "activityText";
			case VIDEO:
				return "activityVideo";
			case SLIDESHARE:
				return "activitySlide";
			case EXTERNAL_TOOL:
				return "activityExternal";
			default:
				return "";	
		}
	}
	
	public String getStyleClassBasedOnNotificationType(NotificationType type) {
		if (type == null) {
			return "";
		}
		switch(type) {
			case Follow_User:
				return "notifFollowed";
			case Comment:
				return "notifComment";
			case AnnouncementPublished:
				return "notifAnnouncement";
			case Comment_Like:
				return "notifLikeComment";
			case Assessment_Approved:
			case Assessment_Comment:
			case Assessment_Requested:
				return "notifAssessment";
			case Mention:
				return "notifMention";
			default:
				return "";	
		}
	}
	
	public String getPreviewLinkStyleClassBasedOnImageSize(ImageSize size) {
		if(size == null) {
			return "";
		}
		switch(size) {
			case Large:
				return "previewLinkBig";
			case Small:
				return "previewLinkSmall";
			default:
				return "";
		}
	}
	
//	public String getClassForSocialActivityText(SocialActivityType type) {
//		if(type == SocialActivityType.Comment) {
//			return "commentPreview";
//		} 
//		return "";
//	}
	
	public String getIconStyleClassBasedOnMediaType(MediaType1 type) {
		switch(type) {
			case Credential:
				return "iconCredential";
			case CompetenceComment:
			case Competence:
				return "iconCompetence";
			default:
				return "";	
		}
	}
	
	public String getStyleClassBasedOnActivityTypeForStatusWall(ActivityType type) {
		switch(type) {
			case TEXT:
				return "iconActivityText";
			case VIDEO:
				return "iconActivityVideo";
			case SLIDESHARE:
				return "iconActivitySlide";
			case EXTERNAL_TOOL:
				return "iconActivityExternal";
			default:
				return "";	
		}
	}
	
	public String getStyleClassBasedOnDeliveryStatus(CredentialDeliveryStatus status) {
		switch(status) {
			case PENDING:
				return "statusOrange";
			case ACTIVE:
				return "statusGreen";
			case ENDED:
				return "statusRed";
			default:
				return "";	
		}
	}

}
