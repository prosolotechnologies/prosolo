package org.prosolo.services.assessment.data.factory;

import org.prosolo.common.domainmodel.assessment.Assessment;
import org.prosolo.common.domainmodel.assessment.AssessmentStatus;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.ActivityAssessmentsSummaryData;
import org.prosolo.services.assessment.data.AssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentsSummaryData;
import org.prosolo.services.assessment.data.CredentialAssessmentsSummaryData;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.OptionalInt;

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
        credAssessmentsSummary.getCredentialIdData().setId(cred.getId());
        credAssessmentsSummary.getCredentialIdData().setTitle(cred.getTitle());
        credAssessmentsSummary.getCredentialIdData().setOrder(cred.getDeliveryOrder());
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

    public AssessmentData getAssessmentData(Assessment assessment, User student, User assessor) {
        return getAssessmentData(assessment.getId(), assessment.getStatus(), assessment.getDateCreated(), assessment.getQuitDate(), assessment.isApproved(), assessment.getDateApproved(), student, assessor, assessment.getBlindAssessmentMode());
    }

    public AssessmentData getAssessmentData(
            long assessmentId, AssessmentStatus status, Date dateCreated, Date dateQuit, boolean approved, Date dateSubmitted, User student, User assessor, BlindAssessmentMode blindAssessmentMode) {
        AssessmentData data = new AssessmentData();
        data.setAssessmentId(assessmentId);
        data.setStatus(status);
        data.setDateRequested(DateUtil.getMillisFromDate(dateCreated));
        data.setDateQuit(DateUtil.getMillisFromDate(dateQuit));
        data.setApproved(approved);
        data.setDateSubmitted(DateUtil.getMillisFromDate(dateSubmitted));
        if (student != null) {
            data.setStudentFullName(student.getName() + " " + student.getLastname());
            data.setStudentAvatarUrl(AvatarUtils.getAvatarUrlInFormat(student, ImageFormat.size120x120));
            data.setStudentId(student.getId());
        }
        if (assessor != null) {
            data.setAssessorFullName(assessor.getName()+ " " + assessor.getLastname());
            data.setAssessorAvatarUrl(AvatarUtils.getAvatarUrlInFormat(assessor, ImageFormat.size120x120));
            data.setAssessorId(assessor.getId());
        }
        data.setBlindAssessmentMode(blindAssessmentMode);

        return data;
    }
}
