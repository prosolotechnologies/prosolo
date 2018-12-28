package org.prosolo.web.assessments.util;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.GradingMode;
import org.prosolo.services.assessment.data.grading.RubricGradeData;
import org.prosolo.services.nodes.data.LearningResourceType;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-03-13
 * @since 1.2.0
 */
public class AssessmentUtil {

    public static boolean isUserAllowedToSeeRubric(GradeData gradeData, LearningResourceType resourceType) {
        //temporary solution for credential and competency before we introduce visibility options for these resources
        //for now students are allowed to see rubric when they are assessed
        if (resourceType == LearningResourceType.CREDENTIAL || resourceType == LearningResourceType.COMPETENCE) {
            return gradeData.isAssessed();
        }
        if (gradeData instanceof RubricGradeData) {
            RubricGradeData rubricGradeData = (RubricGradeData) gradeData;
            return rubricGradeData.getRubricVisibilityForStudent() != null && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.ALWAYS
                    || (rubricGradeData.isAssessed() && rubricGradeData.getRubricVisibilityForStudent() == ActivityRubricVisibility.AFTER_GRADED);
        }
        return false;
    }

    public static boolean isPeerAssessmentEnabled(List<AssessmentTypeConfig> assessmentTypesConfig) {
        return isAssessmentTypeEnabled(assessmentTypesConfig, AssessmentType.PEER_ASSESSMENT);
    }

    public static boolean isSelfAssessmentEnabled(List<AssessmentTypeConfig> assessmentTypesConfig) {
        return isAssessmentTypeEnabled(assessmentTypesConfig, AssessmentType.SELF_ASSESSMENT);
    }

    public static boolean isAssessmentTypeEnabled(List<AssessmentTypeConfig> assessmentTypesConfig, AssessmentType type) {
        if (assessmentTypesConfig == null || assessmentTypesConfig.isEmpty()) {
            return false;
        }
        AssessmentTypeConfig aType = assessmentTypesConfig.stream().filter(t -> t.getType() == type).findFirst().get();
        return aType.isEnabled();
    }

    public static BlindAssessmentMode getBlindAssessmentMode(List<AssessmentTypeConfig> assessmentTypesConfig, AssessmentType type) {
        if (assessmentTypesConfig == null || assessmentTypesConfig.isEmpty()) {
            return BlindAssessmentMode.OFF;
        }
        AssessmentTypeConfig aType = assessmentTypesConfig.stream().filter(t -> t.getType() == type).findFirst().get();
        return aType.getBlindAssessmentMode();
    }


    public static boolean isCredentialFullyGraded(AssessmentDataFull credentialAssessment) {
        if (credentialAssessment.getGradeData().getGradingMode() != GradingMode.NONGRADED && !credentialAssessment.getGradeData().isAssessed()) {
            return false;
        }
        return credentialAssessment.getCompetenceAssessmentData()
                .stream()
                .allMatch(compAssessment -> isCompetenceFullyGraded(compAssessment));
    }

    public static boolean isCompetenceFullyGraded(CompetenceAssessmentData competenceAssessmentData) {
        if (competenceAssessmentData.getGradeData().getGradingMode() != GradingMode.NONGRADED && !competenceAssessmentData.getGradeData().isAssessed()) {
            return false;
        }
        if (competenceAssessmentData.getLearningPathType() == LearningPathType.ACTIVITY) {
            return competenceAssessmentData.getActivityAssessmentData()
                    .stream()
                    .allMatch(actAssessment -> actAssessment.getGrade().getGradingMode() == GradingMode.NONGRADED || actAssessment.getGrade().isAssessed());
        }
        return true;
    }
}
