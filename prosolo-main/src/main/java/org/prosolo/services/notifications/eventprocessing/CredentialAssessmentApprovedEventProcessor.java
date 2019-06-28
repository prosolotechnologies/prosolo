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

public class CredentialAssessmentApprovedEventProcessor extends AssessmentNotificationEventProcessor {

    private static Logger logger = Logger.getLogger(CredentialAssessmentApprovedEventProcessor.class);

    protected long credentialId;
    private AssessmentBasicData credentialAssessment;

    public CredentialAssessmentApprovedEventProcessor(Event event, NotificationManager notificationManager,
                                                      NotificationsSettingsManager notificationsSettingsManager,
                                                      UrlIdEncoder idEncoder, AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        this.credentialAssessment = assessmentManager.getBasicAssessmentInfoForCredentialAssessment(event.getObject().getId());

        credentialId = credentialAssessment.getCredentialId();
    }

    @Override
    boolean isConditionMet(long sender, long receiver) {
        // notification should not be sent in case of a self-assessment
        return sender != receiver;
    }

    @Override
    List<NotificationReceiverData> getReceiversData() {
        PageSection pageSection = PageSection.STUDENT;

        return List.of(new NotificationReceiverData(event.getTarget().getId(), getNotificationLink(pageSection), false, pageSection));
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
        return NotificationType.Assessment_Approved;
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
            case INSTRUCTOR_ASSESSMENT: {
                return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()),
                        assessmentType,
                        section);
            }
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessmentType " + assessmentType);
        }
    }

}
