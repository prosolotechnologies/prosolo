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
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

public abstract class AssessmentCommentEventProcessor extends NotificationEventProcessor {
	
	private static Logger logger = Logger.getLogger(AssessmentCommentEventProcessor.class);
	
	protected AssessmentManager assessmentManager;

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
		List<Long> participantIds;
		AssessmentBasicData assessmentInfo;
		try {
			participantIds = getParticipantIds(assessmentId);
			assessmentInfo = getBasicAssessmentInfo(assessmentId);
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
			PageSection section = studentSection ? PageSection.STUDENT : PageSection.MANAGE;
			String link = getNotificationLink(section);
			boolean isObjectOwner = id == assessmentInfo.getStudentId();
			receivers.add(new NotificationReceiverData(id, link, isObjectOwner, section));
		}
		return receivers;
	}

	protected abstract List<Long> getParticipantIds(long assessmentId);
	protected abstract AssessmentBasicData getBasicAssessmentInfo(long assessmentId);
	protected abstract long getCredentialId(Event event);
	protected abstract long getCredentialAssessmentId(Event event);

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

	private String getNotificationLink(PageSection section) {
		return section.getPrefix() + "/credentials/" +
				idEncoder.encodeId(getCredentialId(event)) +
				"/assessments/" +
				idEncoder.encodeId(getCredentialAssessmentId(event));
	}

}
