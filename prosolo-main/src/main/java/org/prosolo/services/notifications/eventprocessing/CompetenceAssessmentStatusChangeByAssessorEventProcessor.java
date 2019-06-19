package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public abstract class CompetenceAssessmentStatusChangeByAssessorEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CompetenceAssessmentStatusChangeByAssessorEventProcessor.class);

    public CompetenceAssessmentStatusChangeByAssessorEventProcessor(Event event, NotificationManager notificationManager,
                                                                    NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                                    AssessmentManager assessmentManager) {
        super(event, event.getObject().getId(), notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);
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
            case INSTRUCTOR_ASSESSMENT:
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

}
