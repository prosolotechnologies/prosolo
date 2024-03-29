package org.prosolo.web.util.style;

import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricAssessmentGradeSummary;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
import org.prosolo.services.nodes.data.credential.CredentialDeliveryStatus;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;

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
			case GradeAdded:
			case ASSESSMENT_REQUEST_ACCEPTED:
			case ASSESSMENT_REQUEST_DECLINED:
			case ASSESSOR_WITHDREW_FROM_ASSESSMENT:
			case ASSESSOR_ASSIGNED_TO_ASSESSMENT:
			case ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR:
			case ASSESSMENT_REQUEST_EXPIRED:
			case ASSESSMENT_TOKENS_NUMBER_UPDATED:
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

	public String getEvidenceFileTypeIcon(String url) {
		//TODO if there is no extension this logic for extracting extension would not work
		String extension = url.substring(url.lastIndexOf(".") + 1).toLowerCase();

		switch (extension) {
			case "txt":
			case "doc":
			case "docx":
			case "odt":
			case "rtf":
			case "pdf":
				return "evidenceDoc";
			case "jpg":
			case "jpeg":
			case "png":
			case "gif":
			case "tiff":
			case "bmp":
				return "evidenceImage";
			case "mp3":
			case "wav":
			case "3gp":
			case "aac":
			case "wma":
				return "evidenceAudio";
			case "mp4":
			case "avi":
			case "flv":
			case "mov":
			case "mpg":
			case "mpeg":
			case "wmv":
				return "evidenceVideoAlt";
			case "zip":
			case "rar":
			case "gz":
			case "7z":
				return "evidenceArchive";
			default:
				return "evidenceLink";
		}
	}

	public String getGradeStarClass(AssessmentGradeSummary gradeSummary, String nongradedClass, boolean returnGradeClass) {
		if (gradeSummary == null) {
			return getGradeStarClass(0, 0, nongradedClass, returnGradeClass);
		}
		return getGradeStarClass(gradeSummary.getGrade(), gradeSummary.getOutOf(), nongradedClass, returnGradeClass);
	}

	public String getGradeStarClass(int pointsAchieved, int maxPoints, String nongradedClass, boolean returnGradeClass) {
		if (pointsAchieved <= 0) {
			return nongradedClass;
		}
		return returnGradeClass ? "rubricStars has" + maxPoints + "Stars rubricStar0" + pointsAchieved : "";
	}

	public String getGradeStarClassForGradeData(org.prosolo.services.user.data.profile.grade.GradeData gradeData, String nongradedClass, boolean returnGradeClass) {
		if (gradeData == null) {
			return getGradeStarClass(0, 0, nongradedClass, returnGradeClass);
		}
		return getGradeStarClass(gradeData.getScaledGrade(), gradeData.getScaledMaxGrade(), nongradedClass, returnGradeClass);
	}

	public String getGradeStarClassForGradeData(org.prosolo.services.user.data.profile.grade.GradeData gradeData) {
		return getGradeStarClassForGradeData(gradeData, "", true);
	}

	public String getGradeStarClass(AssessmentGradeSummary gradeSummary) {
		return getGradeStarClass(gradeSummary, "", true);
	}

	public String getEmptyStarClass(AssessmentGradeSummary gradeSummary) {
		return getGradeStarClass(gradeSummary, "starEmpty",false);
	}

	public String getEmptyStarClassForGradeData(org.prosolo.services.user.data.profile.grade.GradeData gradeData) {
		return getGradeStarClassForGradeData(gradeData, "starEmpty",false);
	}

	public String getRubricAssessmentStarLabel(RubricAssessmentGradeSummary gradeSummary) {
		return gradeSummary != null
				? gradeSummary.getGrade() > 0
						? gradeSummary.getGradeLevelTitle() + "<br>(level " + gradeSummary.getGrade() + "/" + gradeSummary.getOutOf() + ")"
						: ""
				: "";
	}

	public String getAssessmentStarTooltip(GradeData gradeData, String resType) {
		if (!gradeData.isAssessed()) {
			return "No grade";
		}

		if (gradeData.getGradingMode() == GradingMode.AUTOMATIC && ("CREDENTIAL".equals(resType) || "COMPETENCE".equals(resType))) {
			return "Sum of " +
					("CREDENTIAL".equals(resType)
							? ResourceBundleUtil.getLabel("competence").toLowerCase()
							: "activity") + " points";
		}

		if (gradeData.getGradingMode() == GradingMode.MANUAL_RUBRIC) {
			return getRubricAssessmentStarLabel((RubricAssessmentGradeSummary) gradeData.getAssessmentStarData());
		}

		return "";
	}

	public String getSocialNetworkClass(SocialNetworkName socialNetwork) {
		if (socialNetwork == null) {
			return "";
		}
		switch (socialNetwork) {
			case BLOG:
				return "website";
			case FACEBOOK:
				return "facebook";
			case GPLUS:
				return "gplus";
			case LINKEDIN:
				return "linkedIn";
			case TWITTER:
				return "twitter";
			default:
				return "";
		}
	}

	public String getIconClassForAssessmentStatus(AssessmentStatus status) {
		if (status == null) {
			return "";
		}
		switch (status) {
			case REQUESTED:
				return "tagPending";
			case REQUEST_EXPIRED:
				return "tagDeclined";
			case REQUEST_DECLINED:
				return "tagDeclined";
			case PENDING:
				return "tagPending";
			case SUBMITTED:
				return "tagApproved";
			case ASSESSMENT_QUIT:
				return "tagDeclined";
			case SUBMITTED_ASSESSMENT_QUIT:
				return "tagDeclined";
			default:
				return "";
		}
	}

}
