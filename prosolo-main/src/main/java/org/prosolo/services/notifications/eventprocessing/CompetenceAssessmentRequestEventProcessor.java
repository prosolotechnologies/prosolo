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
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public class CompetenceAssessmentRequestEventProcessor extends CompetenceAssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CompetenceAssessmentRequestEventProcessor.class);

	public CompetenceAssessmentRequestEventProcessor(Event event, NotificationManager notificationManager,
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
        PageSection section = competenceAssessment.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT ? PageSection.MANAGE : PageSection.STUDENT;

        return List.of(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(),false, section));
    }

    @Override
    NotificationType getNotificationType() {
        return NotificationType.Assessment_Requested;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Competence;
    }

    @Override
    long getObjectId() {
        return competenceId;
    }

    private String getNotificationLink() {
        AssessmentType assessmentType = competenceAssessment.getType();

        switch (assessmentType) {
            case INSTRUCTOR_ASSESSMENT:
                return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(competenceAssessment.getCredentialAssessmentId()),
                        assessmentType,
                        PageSection.MANAGE);
            case PEER_ASSESSMENT:
                return "/assessments/my/competences/" + idEncoder.encodeId(competenceAssessment.getCompetenceAssessmentId());
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
	}

}
