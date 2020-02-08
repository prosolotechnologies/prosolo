package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class CredentialGradeAddedEventProcessor extends GradeAddedEventProcessor {

    private static Logger logger = Logger.getLogger(CredentialGradeAddedEventProcessor.class);

    private long credentialId;
    private AssessmentBasicData credentialAssessment;

    public CredentialGradeAddedEventProcessor(Event event, NotificationManager notificationManager,
                                              NotificationsSettingsManager notificationsSettingsManager,
                                              UrlIdEncoder idEncoder, AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        credentialAssessment = assessmentManager.getBasicAssessmentInfoForCredentialAssessment(event.getObject().getId());

        credentialId = credentialAssessment.getCredentialId();
    }

    @Override
    protected boolean shouldNotificationBeGenerated() {
        return credentialAssessment.getType() != AssessmentType.SELF_ASSESSMENT;
    }

    @Override
    protected long getStudentId() {
        return credentialAssessment.getStudentId();
    }

    @Override
    protected BlindAssessmentMode getBlindAssessmentMode() {
        return credentialAssessment.getBlindAssessmentMode();
    }

    @Override
    protected long getAssessorId() {
        return credentialAssessment.getAssessorId();
    }

    @Override
    protected String getNotificationLink() {
        AssessmentType assessmentType = credentialAssessment.getType();

        switch (assessmentType) {
            case INSTRUCTOR_ASSESSMENT:
            case PEER_ASSESSMENT:
                return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()),
                        assessmentType,
                        PageSection.STUDENT);
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Credential;
    }

    @Override
    long getObjectId() {
        return credentialId;
    }

}
