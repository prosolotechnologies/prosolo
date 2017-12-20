package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.event.Event;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;

import java.util.ArrayList;
import java.util.List;

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
		List<Long> participantIds;
		AssessmentBasicData assessmentInfo;
		try {
			participantIds = assessmentManager.getParticipantIds(assessmentId);
			assessmentInfo = assessmentManager.getBasicAssessmentInfoForActivityAssessment(
					assessmentId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return new ArrayList<>();
		}
		for (long id : participantIds) {
			/*
			 * assessed user is a student and assessor can be student or manager (if it is default
			 * assessment, assessor is manager, otherwise assessor is student) and all other participants
			 * are managers
			 */
			//TODO check if it is valid assumption that only Instrucor assessment assessor should be led to manage section and all others to student section
			boolean studentSection = id == assessmentInfo.getStudentId()
					|| (assessmentInfo.getType() != AssessmentType.INSTRUCTOR_ASSESSMENT && id == assessmentInfo.getAssessorId());
			String prefix = studentSection ? "" : "/manage";
			boolean isObjectOwner = id == assessmentInfo.getStudentId();
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
