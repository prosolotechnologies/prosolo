package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.data.NotificationReceiverData;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

import java.util.List;

public class CredentialAssessmentRequestEventProcessor extends AssessmentNotificationEventProcessor {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CredentialAssessmentRequestEventProcessor.class);

    private long credentialId;
    private AssessmentBasicData credentialAssessment;

    public CredentialAssessmentRequestEventProcessor(Event event, NotificationManager notificationManager,
                                                     NotificationsSettingsManager notificationsSettingsManager,
                                                     UrlIdEncoder idEncoder, AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        credentialAssessment = assessmentManager.getBasicAssessmentInfoForCredentialAssessment(event.getObject().getId());

        credentialId = credentialAssessment.getCredentialId();
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        return true;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        PageSection section = credentialAssessment.getType() == AssessmentType.INSTRUCTOR_ASSESSMENT ? PageSection.MANAGE : PageSection.STUDENT;

        return List.of(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(section), false, section));
    }

    @Override
    protected long getAssessorId() {
        return credentialAssessment.getAssessorId();
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
    NotificationType getNotificationType() {
        return NotificationType.Assessment_Requested;
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.Credential;
    }

    @Override
    long getObjectId() {
        return credentialId;
    }

    private String getNotificationLink(PageSection section) {
        AssessmentType assessmentType = credentialAssessment.getType();

        switch (assessmentType) {
            case INSTRUCTOR_ASSESSMENT:
                return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()),
                        assessmentType,
                        section);
            case PEER_ASSESSMENT:
                return AssessmentLinkUtil.getCredentialAssessmentUrlForStudentPeerAssessor(
                        idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()));
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
    }

}
