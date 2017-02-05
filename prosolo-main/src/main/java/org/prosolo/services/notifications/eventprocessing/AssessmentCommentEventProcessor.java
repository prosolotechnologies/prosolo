package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.RoleNames;

public class AssessmentCommentEventProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(AssessmentCommentEventProcessor.class);
	
	private RoleManager roleManager;

	public AssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
			RoleManager roleManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.roleManager = roleManager;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return sender != receiver;
	}

	@Override
	List<Long> getReceiverIds() {
		List<Long> users = new ArrayList<>();
		try {
			users.add(event.getTarget().getId());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return users;
	}

	@Override
	long getSenderId() {
		return event.getActorId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Comment;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.CredentialAssessment;
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}

	@Override
	String getNotificationLink() {
		return "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(event.getObject().getId());
		//boolean isRecieverAssessor = Boolean.parseBoolean(event.getParameters().get("isRecepientAssessor"));
//		if (isRecieverAssessor) {
//			//this notification will be read by assessor, prefix url with "manage"
////			return "/manage/credential-assessment.xhtml?id=" +
////				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
////				"&assessmentId=" +
////				idEncoder.encodeId(event.getObject().getId());
//			return "/manage/credentials/" +
//				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
//				"/assessments/" +
//				idEncoder.encodeId(event.getObject().getId());
//		}
//		else {
////			return "credential-assessment.xhtml?id=" +
////					idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
////					"&assessmentId=" +
////					idEncoder.encodeId(event.getObject().getId());
//			return "/credentials/" +
//					idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
//					"/assessments/" +
//					idEncoder.encodeId(event.getObject().getId());
//		}
	}
	
	@Override
	protected String getUrlSection(long userId) {
		List<String> roles = new ArrayList<>();
		roles.add(RoleNames.MANAGER);
		roles.add(RoleNames.INSTRUCTOR);
		boolean hasManagerOrInstructorRole = roleManager.hasAnyRole(userId, roles);
		if (hasManagerOrInstructorRole) {
			return "/manage";
		}
		return "";
	}

}
