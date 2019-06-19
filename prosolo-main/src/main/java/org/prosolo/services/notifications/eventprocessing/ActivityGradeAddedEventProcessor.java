package org.prosolo.services.notifications.eventprocessing;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.event.Event;
import org.prosolo.common.web.ApplicationPage;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentBasicData;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.services.notifications.eventprocessing.util.AssessmentLinkUtil;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.util.page.PageSection;

public class ActivityGradeAddedEventProcessor extends GradeAddedEventProcessor {

    private static Logger logger = Logger.getLogger(ActivityGradeAddedEventProcessor.class);

    private AssessmentBasicData activityAssessment;
    private AssessmentBasicData competenceAssessment;
    private long credentialId;
    private long competenceId;
    private ActivityData activityData;

    public ActivityGradeAddedEventProcessor(Event event, NotificationManager notificationManager,
                                            NotificationsSettingsManager notificationsSettingsManager, UrlIdEncoder idEncoder,
                                            AssessmentManager assessmentManager, Activity1Manager activityManager) {
        super(event, notificationManager, notificationsSettingsManager, idEncoder);

        activityAssessment = assessmentManager.getBasicAssessmentInfoForActivityAssessment(event.getObject().getId());
        competenceAssessment = assessmentManager.getBasicAssessmentInfoForCompetenceAssessment(activityAssessment.getCompetenceAssessmentId());

        credentialId = activityAssessment.getCredentialId();
        competenceId = activityAssessment.getCompetenceId();
        long activityId = activityAssessment.getActivityId();

        activityData = activityManager.getActivityData(credentialId, competenceId, activityId, false, false);
    }

    @Override
    protected boolean shouldNotificationBeGenerated() {
        //notification should not be generated for self assessment and auto graded assessment
        return activityAssessment.getType() != AssessmentType.SELF_ASSESSMENT && activityData.getAssessmentSettings().getGradingMode() != GradingMode.AUTOMATIC;
    }

    @Override
    protected long getStudentId() {
        return activityAssessment.getStudentId();
    }

    @Override
    protected BlindAssessmentMode getBlindAssessmentMode() {
        return competenceAssessment.getBlindAssessmentMode();
    }

    @Override
    protected long getAssessorId() {
        return activityAssessment.getAssessorId();
    }

    @Override
    protected String getNotificationLink() {
        ApplicationPage page = ApplicationPage.getPageForURI(event.getPage());

        AssessmentType assessmentType = activityAssessment.getType();

        switch (assessmentType) {
            case INSTRUCTOR_ASSESSMENT:
                return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                        idEncoder.encodeId(credentialId),
                        idEncoder.encodeId(activityAssessment.getCredentialAssessmentId()),
                        activityAssessment.getType(),
                        PageSection.STUDENT);
            case PEER_ASSESSMENT:
                switch (page) {
                    // if grade is added to a competency assessment is a part of the credential assessment
                    case MY_ASSESSMENTS_CREDENTIAL_ASSESSMENT:
                        return AssessmentLinkUtil.getCredentialAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(activityAssessment.getCredentialAssessmentId()),
                                activityAssessment.getType(),
                                PageSection.STUDENT);
                    // if grade is added to a competency assessment is a part of the competency assessment
                    case MY_ASSESSMENTS_COMPETENCE_ASSESSMENT:
                        return AssessmentLinkUtil.getCompetenceAssessmentUrlForAssessedStudent(
                                idEncoder.encodeId(credentialId),
                                idEncoder.encodeId(competenceId),
                                idEncoder.encodeId(competenceAssessment.getCompetenceAssessmentId()),
                                activityAssessment.getType(),
                                PageSection.STUDENT);
                }
            default:
                throw new IllegalArgumentException("Cannot generate notification link for the assessment type " + assessmentType);
        }
    }

    @Override
    ResourceType getObjectType() {
        return ResourceType.ActivityAssessment;
    }

}
