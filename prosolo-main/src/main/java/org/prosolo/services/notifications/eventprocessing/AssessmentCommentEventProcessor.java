package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class AssessmentCommentEventProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(AssessmentCommentEventProcessor.class);

	public AssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return sender != receiver;
	}

	@Override
	List<Long> getReceiverIds() {
		List<Long> users = new ArrayList<>();
		try {
			users.add(event.getObject().getId());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return users;
	}

	@Override
	long getSenderId() {
		return event.getActor().getId();
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.Assessment_Comment;
	}

	@Override
	ObjectType getObjectType() {
		return ObjectType.CredentialAssessment;
	}

	@Override
	long getObjectId() {
		return event.getTarget().getId();
	}

	@Override
	String getNotificationLink() {
		boolean isRecieverAssessor = Boolean.parseBoolean(event.getParameters().get("isRecepientAssessor"));
		if(isRecieverAssessor) {
			//this notification will be read by assessor, prefix url with "manage"
			return "/manage/credential-assessment.xhtml?id=" +
					idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
					"&assessmentId=" +
					idEncoder.encodeId(event.getTarget().getId());
		}
		else {
			return "credential-assessment.xhtml?id=" +
					idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
					"&assessmentId=" +
					idEncoder.encodeId(event.getTarget().getId());
		}
	}

}
