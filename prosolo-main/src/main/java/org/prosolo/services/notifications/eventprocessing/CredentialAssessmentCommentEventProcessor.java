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

import java.util.List;

public class CredentialAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

    private static Logger logger = Logger.getLogger(CredentialAssessmentCommentEventProcessor.class);

    private long credentialId;
    private AssessmentBasicData credentialAssessment;

    public CredentialAssessmentCommentEventProcessor(Event event, NotificationManager notificationManager,
                                                     NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                     AssessmentManager assessmentManager)  {
        super(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);

        credentialAssessment = assessmentManager.getBasicAssessmentInfoForCredentialAssessment(event.getTarget().getId());

        credentialId = credentialAssessment.getCredentialId();
    }

    @Override
    protected List<Long> getParticipantIds(long assessmentId) {
        return assessmentManager.getCredentialDiscussionParticipantIds(assessmentId);
    }

    @Override
    protected AssessmentBasicData getBasicAssessmentInfo() {
        return credentialAssessment;
    }

    @Override
    protected ResourceType getObjectType() {
        return ResourceType.Credential;
    }

    @Override
    protected long getObjectId() {
        return credentialId;
    }

    @Override
    protected String getNotificationLink(PageSection section) {
        ApplicationPage page = ApplicationPage.getPageForURI(event.getPage());

        AssessmentType assessmentType = credentialAssessment.getType();

        switch (assessmentType) {
            // if a comment is created as a part of the instructor assessment
            case INSTRUCTOR_ASSESSMENT: {
                switch (page) {
                    // by the instructor (assessor)
                    case MANAGE_CREDENTIAL_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()),
                                AssessmentType.INSTRUCTOR_ASSESSMENT,
                                PageSection.STUDENT);

                    // by the student (who is being assessed)
                    case CREDENTIAL_INSTRUCTOR_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(credentialAssessment.getCredentialAssessmentId()),
                                AssessmentType.INSTRUCTOR_ASSESSMENT,
                                PageSection.MANAGE);
                }
            }
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessmentType " + assessmentType);
        }
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
    protected long getStudentId() {
        return credentialAssessment.getStudentId();
    }

}
