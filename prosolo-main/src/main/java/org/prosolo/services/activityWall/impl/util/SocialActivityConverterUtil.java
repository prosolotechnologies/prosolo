package org.prosolo.services.activityWall.impl.util;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.activitywall.CourseSocialActivity;
import org.prosolo.common.domainmodel.activitywall.GoalNoteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.NodeSocialActivity;
import org.prosolo.common.domainmodel.activitywall.NodeUserSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.web.activitywall.data.PublishingServiceData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class SocialActivityConverterUtil {
	
	private static Logger logger = Logger.getLogger(SocialActivityConverterUtil.class);
	
	public static Class<? extends Node> resolveNodeClass(String dtype) {
		switch (dtype) {
			case "LearningGoal":
				return LearningGoal.class;
			case "TargetLearningGoal":
				return TargetLearningGoal.class;
			case "Competence":
				return Competence.class;
			case "TargetCompetence":
				return TargetCompetence.class;
			case "ResourceActivity":
				return ResourceActivity.class;
			case "TargetActivity":
				return TargetActivity.class;
			case "UploadAssignmentActivity":
				return UploadAssignmentActivity.class;
			default:
				return null;
		}
	}
	
	public static Class<? extends SocialActivity> resolveSocialActivityClass(String dtype) {
		switch (dtype) {
			case "NodeSocialActivity":
				return NodeSocialActivity.class;
			case "TwitterPostSocialActivity":
				return TwitterPostSocialActivity.class;
			case "PostSocialActivity":
				return PostSocialActivity.class;
			case "CourseSocialActivity":
				return CourseSocialActivity.class;
			case "GoalNoteSocialActivity":
				return GoalNoteSocialActivity.class;
			case "NodeUserSocialActivity":
				return NodeUserSocialActivity.class;
			default:
				return null;
		}
	}
	
	public static PublishingServiceData getPublishingServiceData(ServiceType serviceType, String nickname, String profileUrl,
			Locale locale) {
		return new PublishingServiceData(
				getPublishingService(locale, serviceType),
				nickname,
				profileUrl);
	}
	
	public static String getPublishingService(Locale locale, ServiceType service) {
		String publishingService = "";
		
		if (service != null) {
			try {
				String key = "activitywall.publishingService." + service.name();
				publishingService = ResourceBundleUtil.getMessage( 
						key,
						locale);
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
		}
		return publishingService;
	}
}
