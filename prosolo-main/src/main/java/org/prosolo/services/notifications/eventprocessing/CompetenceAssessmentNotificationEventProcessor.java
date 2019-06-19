package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public abstract class CompetenceAssessmentNotificationEventProcessor extends AssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CompetenceAssessmentNotificationEventProcessor.class);

    protected AssessmentBasicData competenceAssessment;
    protected long credentialId;
    protected long competenceId;

    public CompetenceAssessmentNotificationEventProcessor(Event event, long competenceAssessmentId, NotificationManager notificationManager,
                                                          NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                          AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        competenceAssessment = assessmentManager.getBasicAssessmentInfoForCompetenceAssessment(competenceAssessmentId);

        credentialId = competenceAssessment.getCredentialId();
        competenceId = competenceAssessment.getCompetenceId();
    }

    @Override
    protected long getAssessorId() {
        return competenceAssessment.getAssessorId();
    }

    @Override
    protected long getStudentId() {
        return competenceAssessment.getStudentId();
    }

    @Override
    protected BlindAssessmentMode getBlindAssessmentMode() {
        return competenceAssessment.getBlindAssessmentMode();
    }

}
