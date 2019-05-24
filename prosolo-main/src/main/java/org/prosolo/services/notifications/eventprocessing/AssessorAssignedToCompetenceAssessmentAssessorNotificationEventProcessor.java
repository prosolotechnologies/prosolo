package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes event when assessor is assigned to existing competency assessment and prepares data
 * for generating notification for ASSESSOR
 *
 * @author stefanvuckovic
 * @date 2019-04-23
 * @since 1.3.2
 */
public class AssessorAssignedToCompetenceAssessmentAssessorNotificationEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AssessorAssignedToCompetenceAssessmentAssessorNotificationEventProcessor.class);

	public AssessorAssignedToCompetenceAssessmentAssessorNotificationEventProcessor(Event event, Session session, NotificationManager notificationManager,
                                                                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                                                    ContextJsonParserService ctxJsonParserService) {
		super(event, event.getTarget().getId(), session, notificationManager, notificationsSettingsManager, idEncoder, ctxJsonParserService);
	}

	@Override
	boolean isConditionMet(long sender, long receiver) {
		return true;
	}

	@Override
	List<NotificationReceiverData> getReceiversData() {
		List<NotificationReceiverData> receivers = new ArrayList<>();
		receivers.add(new NotificationReceiverData(getAssessorId(), getNotificationLink(),
				false, PageSection.STUDENT));
		return receivers;
	}

	@Override
	NotificationType getNotificationType() {
		return NotificationType.ASSIGNED_TO_ASSESSMENT_AS_ASSESSOR;
	}

	@Override
	ResourceType getObjectType() {
		return ResourceType.Student;
	}

	@Override
	long getObjectId() {
		return getStudentId();
	}

	@Override
	public ResourceType getTargetType() {
		return ResourceType.Competence;
	}

	@Override
	public long getTargetId() {
		return getAssessment().getCompetence().getId();
	}

	private String getNotificationLink() {
		return AssessmentLinkUtil.getCompetenceAssessmentNotificationLinkForStudent(
				getCredentialId(), getAssessment().getCompetence().getId(), getAssessment().getId(), getAssessment().getType(), idEncoder);
	}

}
