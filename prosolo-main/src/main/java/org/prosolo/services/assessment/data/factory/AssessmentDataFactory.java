package org.prosolo.services.assessment.data.factory;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.services.assessment.data.ActivityAssessmentsSummaryData;
import org.prosolo.services.assessment.data.CompetenceAssessmentsSummaryData;
import org.prosolo.services.assessment.data.CredentialAssessmentsSummaryData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-10-26
 * @since 1.1.0
 */
@Component
public class AssessmentDataFactory implements Serializable {

    private static final long serialVersionUID = -8747866808673935022L;

    @Inject private ActivityDataFactory activityDataFactory;

    public CredentialAssessmentsSummaryData getCredentialAssessmentsSummary(Credential1 cred) {
        CredentialAssessmentsSummaryData credAssessmentsSummary = new CredentialAssessmentsSummaryData();
        credAssessmentsSummary.setId(cred.getId());
        credAssessmentsSummary.setTitle(cred.getTitle());
        credAssessmentsSummary.setMandatoryOrder(cred.isCompetenceOrderMandatory());

        return credAssessmentsSummary;
    }

    public CompetenceAssessmentsSummaryData getCompetenceAssessmentsSummaryData(
            Competence1 comp, Long numberOfEnrolledStudents, Long numberOfAssessedStudents, Long numberOfNotifications) {
        CompetenceAssessmentsSummaryData compSummary = new CompetenceAssessmentsSummaryData();
        compSummary.setId(comp.getId());
        compSummary.setTitle(comp.getTitle());
        if (numberOfEnrolledStudents != null) {
            compSummary.setNumberOfEnrolledStudents(numberOfEnrolledStudents);
        }
        if (numberOfAssessedStudents != null) {
            compSummary.setNumberOfAssessedStudents(numberOfAssessedStudents);
        }
        if (numberOfNotifications != null) {
            compSummary.setNumberOfNotifications(numberOfNotifications);
        }
        compSummary.setGradingEnabled(comp.getGradingMode() != GradingMode.NONGRADED);
        return compSummary;
    }

    public ActivityAssessmentsSummaryData getActivityAssessmentsSummaryData(
            Activity1 activity, Long numberOfUsersCompletedActivity, Long numberOfAssessedUsers) {
        ActivityAssessmentsSummaryData activitySummary = new ActivityAssessmentsSummaryData();
        activitySummary.setId(activity.getId());
        activitySummary.setTitle(activity.getTitle());
        activitySummary.setActivityType(activityDataFactory.getActivityType(activity));
        activitySummary.setResultType(activity.getResultType());
        activitySummary.setGradingEnabled(activity.getGradingMode() != GradingMode.NONGRADED);
        if (numberOfUsersCompletedActivity != null) {
            activitySummary.setNumberOfStudentsCompletedActivity(numberOfUsersCompletedActivity);
        }
        if (numberOfAssessedUsers != null) {
            activitySummary.setNumberOfAssessedStudents(numberOfAssessedUsers);
        }

        return activitySummary;
    }
}