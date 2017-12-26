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
import org.prosolo.services.nodes.data.assessments.AssessmentBasicData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

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
		for(long id : participantIds) {
			/*
			 * assessed user is a student and assessor can be student or manager (if it is default
			 * assessment, assessor is manager, otherwise assessor is student) and all other participants
			 * are managers
			 */
			boolean studentSection = id == assessmentInfo.getStudentId()
					|| !assessmentInfo.isDefault() && id == assessmentInfo.getAssessorId();
			PageSection section = studentSection ? PageSection.STUDENT : PageSection.MANAGE;
			String link = getNotificationLink(section);
			boolean isObjectOwner = id == assessmentInfo.getStudentId();
			receivers.add(new NotificationReceiverData(id, link, isObjectOwner, section));
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

	private String getNotificationLink(PageSection section) {
		return section.getPrefix() + "/credentials/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialId"))) +
				"/assessments/" +
				idEncoder.encodeId(Long.parseLong(event.getParameters().get("credentialAssessmentId")));
	}

}
