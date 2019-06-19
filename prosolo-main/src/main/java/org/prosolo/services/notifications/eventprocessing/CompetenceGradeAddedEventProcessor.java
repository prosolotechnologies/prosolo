package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class CompetenceGradeAddedEventProcessor extends GradeAddedEventProcessor {

    private static Logger logger = Logger.getLogger(CompetenceGradeAddedEventProcessor.class);

    private long credentialId;
    private long competenceId;
    private AssessmentBasicData competenceAssessment;

    public CompetenceGradeAddedEventProcessor(Event event, NotificationManager notificationManager,
                                              NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                              AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        competenceAssessment = assessmentManager.getBasicAssessmentInfoForCompetenceAssessment(event.getObject().getId());

        credentialId = competenceAssessment.getCredentialId();
        competenceId = competenceAssessment.getCompetenceId();
    }

    @Override
    protected boolean shouldNotificationBeGenerated() {
        return competenceAssessment.getType() != AssessmentType.SELF_ASSESSMENT;
    }

    @Override
    protected long getStudentId() {
        return competenceAssessment.getStudentId();
    }

    @Override
    protected BlindAssessmentMode getBlindAssessmentMode() {
        return competenceAssessment.getBlindAssessmentMode();
    }

    @Override
    protected long getAssessorId() {
        return competenceAssessment.getAssessorId();
    }

    @Override
    protected String getNotificationLink() {
        ApplicationPage page = ApplicationPage.getPageForURI(event.getPage());

        AssessmentType assessmentType = competenceAssessment.getType();

        switch (assessmentType) {
            // if a comment is created as a part of the instructor assessment
            case INSTRUCTOR_ASSESSMENT:
                switch (page) {
                    // by the instructor (assessor)
                    case CREDENTIAL_ASSESSMENT_MANAGE:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(competenceAssessment.getCredentialAssessmentId()),
                                assessmentType,
                                PageSection.STUDENT);
                }
                // if a comment is created as a part of the peer assessment
            case PEER_ASSESSMENT:
                switch (page) {
                    // by the student (assessor) as a part of the credential assessment
                    case MY_ASSESSMENTS_CREDENTIAL_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(competenceAssessment.getCredentialAssessmentId()),
                                assessmentType,
                                PageSection.STUDENT);

                    // by the student (assessor) as a part of the competency assessment
                    case MY_ASSESSMENTS_COMPETENCE_ASSESSMENT:
                        return AssessmentLinkUtil.getCompetenceAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(competenceId),
                                idEncoder.encodeId(competenceAssessment.getCompetenceAssessmentId()),
                                assessmentType,
                                PageSection.STUDENT);
                }
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.CompetenceAssessment;
    }

}
