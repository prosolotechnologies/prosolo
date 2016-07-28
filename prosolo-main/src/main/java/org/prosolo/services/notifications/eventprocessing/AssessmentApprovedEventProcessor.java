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

public class AssessmentApprovedEventProcessor extends NotificationEventProcessor {

	private static Logger logger = Logger.getLogger(AssessmentApprovedEventProcessor.class);

	public AssessmentApprovedEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
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
		return NotificationType.Assessment_Approved;
	}

	@Override
	ObjectType getObjectType() {
		return ObjectType.CredentialAssessment;
	}

	@Override
	long getObjectId() {
		return event.getObject().getId();
	}

	@Override
	String getNotificationLink() {
//		return "credential-assessment.xhtml?id=" +
//				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
//				"&assessmentId=" +
//				idEncoder.encodeId(event.getTarget().getId());
		return "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(event.getObject().getId());
	}

}
