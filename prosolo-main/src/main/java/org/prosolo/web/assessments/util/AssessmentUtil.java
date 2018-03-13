package org.prosolo.web.assessments.util;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.services.assessment.data.grading.GradeData;
import org.prosolo.services.assessment.data.grading.RubricGradeData;
import org.prosolo.services.nodes.data.LearningResourceType;

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
}
