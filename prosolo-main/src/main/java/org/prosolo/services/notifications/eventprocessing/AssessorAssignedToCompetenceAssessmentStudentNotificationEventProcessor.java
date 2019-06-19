package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.data.NotificationSenderData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

/**
 * Processes event when assessor is assigned to existing competency assessment and prepares data
 * for generating notification for STUDENT
 *
 * @author stefanvuckovic
 * @date 2019-04-23
 * @since 1.3.2
 */
public class AssessorAssignedToCompetenceAssessmentStudentNotificationEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AssessorAssignedToCompetenceAssessmentStudentNotificationEventProcessor.class);

    public AssessorAssignedToCompetenceAssessmentStudentNotificationEventProcessor(Event event, NotificationManager notificationManager,
                                                                                   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                                                   AssessmentManager assessmentManager) {
        super(event, event.getTarget().getId(), notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        PageSection pageSection = PageSection.STUDENT;

        return List.of(new NotificationReceiverData(getStudentId(), getNotificationLink(pageSection), false, pageSection));
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.ASSESSOR_ASSIGNED_TO_ASSESSMENT;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Competence;
    }

    @Override
    long getObjectId() {
        return competenceId;
    }

    private String getNotificationLink(PageSection pageSection) {
        AssessmentType assessmentType = competenceAssessment.getType();

        switch (assessmentType) {
            case PEER_ASSESSMENT:
                return AssessmentLinkUtil.getCompetenceAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(competenceId),
                        idEncoder.encodeId(competenceAssessment.getCompetenceAssessmentId()),
                        assessmentType,
                        pageSection);
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
    }

    @Override
    NotificationSenderData getSenderData() {
        return getSenderData(getAssessorId());
    }
}
