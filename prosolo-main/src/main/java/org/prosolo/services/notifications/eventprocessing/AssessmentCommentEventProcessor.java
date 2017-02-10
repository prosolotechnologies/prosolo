package org.prosolo.services.notifications.eventprocessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class AssessmentCommentEventProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(AssessmentCommentEventProcessor.class);
	
	private AssessmentManager assessmentManager;

	public AssessmentCommentEventProcessor(Event event, Session session, NotificationManager notificationManager,
			NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
			AssessmentManager assessmentManager) {
		super(event, session, notificationManager, notificationsSettingsManager, idEncoder);
		this.assessmentManager = assessmentManager;
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return sender != receiver;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		long assessmentId = event.getTarget().getId();
		String link = getNotificationLink();
		List<Long> participantIds = null;
		long assessedStudentId = 0;
		try {
			participantIds = assessmentManager.getParticipantIds(assessmentId);
			assessedStudentId = assessmentManager.getAssessedStudentIdForActivityAssessment(
					assessmentId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
		for(long id : participantIds) {
			/*
			 * this is because out of all regular users, only assessed student
			 * can participate in assessment discussion. All other users are managers.
			 */
			String prefix = id == assessedStudentId ? "" : "/manage";
			boolean isObjectOwner = id == assessedStudentId;
			receivers.add(new NotificationReceiverData(id, prefix + link, isObjectOwner));
		}
		return receivers;
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
		return ResourceType.Credential;
	}

	@Override
	long getObjectId() {
		return Long.parseLong(event.getParameters().get("credentialId"));
	}

	private String getNotificationLink() {
		return "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialAssessmentId")));
	}

}
