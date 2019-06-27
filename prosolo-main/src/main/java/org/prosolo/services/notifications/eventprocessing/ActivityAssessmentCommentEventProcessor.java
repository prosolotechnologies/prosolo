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

public class ActivityAssessmentCommentEventProcessor extends AssessmentCommentEventProcessor {

    private static Logger logger = Logger.getLogger(ActivityAssessmentCommentEventProcessor.class);

    private long credentialId;
    private long competenceId;
    private AssessmentBasicData activityAssessment;

    public ActivityAssessmentCommentEventProcessor(Event event, NotificationManager notificationManager,
                                                   NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                                   AssessmentManager assessmentManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder, assessmentManager);

        activityAssessment = assessmentManager.getBasicAssessmentInfoForActivityAssessment(event.getTarget().getId());
        credentialId = activityAssessment.getCredentialId();
        competenceId = activityAssessment.getCompetenceId();
    }

    @Override
    protected List<Long> getParticipantIds(long assessmentId) {
        return assessmentManager.getActivityDiscussionParticipantIds(assessmentId);
    }

    @Override
    protected AssessmentBasicData getBasicAssessmentInfo() {
        return activityAssessment;
    }

    @Override
    protected BlindAssessmentMode getBlindAssessmentMode() {
        return activityAssessment.getBlindAssessmentMode();
    }

    @Override
    protected long getAssessorId() {
        return getBasicAssessmentInfo().getAssessorId();
    }

    @Override
    protected long getStudentId() {
        return getBasicAssessmentInfo().getStudentId();
    }

    @Override
    protected ResourceType getObjectType() {
		/*
		if credential assessment id is available we generate notification for credential and credential resource type
		is returned, otherwise competence type is returned
		 */
        return activityAssessment.getCredentialAssessmentId() > 0 ? ResourceType.Credential : ResourceType.Competence;
    }

    @Override
    protected long getObjectId() {
        return activityAssessment.getCredentialAssessmentId() > 0 ? credentialId : competenceId;
    }

    @Override
    protected String getNotificationLink(PageSection section) {
        ApplicationPage page = ApplicationPage.getPageForURI(event.getPage());

        AssessmentType assessmentType = activityAssessment.getType();

        switch (assessmentType) {
            // if a comment is created as a part of the instructor assessment
            case INSTRUCTOR_ASSESSMENT: {
                switch (page) {
                    // by the instructor (assessor)
                    case CREDENTIAL_ASSESSMENT_MANAGE:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(activityAssessment.getCredentialAssessmentId()),
                                AssessmentType.INSTRUCTOR_ASSESSMENT,
                                PageSection.STUDENT);

                    // by the student (who is being assessed)
                    case COMPETENCE_INSTRUCTOR_ASSESSMENT:
                    case CREDENTIAL_INSTRUCTOR_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId( activityAssessment.getCredentialAssessmentId()),
                                AssessmentType.INSTRUCTOR_ASSESSMENT,
                                PageSection.MANAGE);
                }
            }
            // if a comment is created as a part of the peer assessment
            case PEER_ASSESSMENT: {
                switch (page) {
                    // by the peer assessor, as a part of the credential assessment
                    case MY_ASSESSMENTS_CREDENTIAL_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(activityAssessment.getCredentialAssessmentId()),
                                AssessmentType.PEER_ASSESSMENT,
                                PageSection.STUDENT);

                    // by the peer assessor, as a part of the competency assessment
                    case MY_ASSESSMENTS_COMPETENCES:
                        return AssessmentLinkUtil.getCompetenceAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(competenceId),
                                idEncoder.encodeId(activityAssessment.getCompetenceAssessmentId()),
                                AssessmentType.PEER_ASSESSMENT,
                                PageSection.STUDENT);

                    // by the student (being assessed), as a part of the credential assessment
                    case CREDENTIAL_PEER_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForStudentPeerAssessor(
                                idEncoder.encodeId(activityAssessment.getCredentialAssessmentId()));

                    // by the student (being assessed), as a part of the competency assessment
                    case COMPETENCE_PEER_ASSESSMENT:
                        return AssessmentLinkUtil.getCompetenceAssessmentUrlForStudentPeerAssessor(
                                idEncoder.encodeId(activityAssessment.getCompetenceAssessmentId()));
                }
            }
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessmentType " + assessmentType);
        }
    }

}
