package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.util.Pair;

/**
 * @author stefanvuckovic
 * @date 2018-03-26
 * @since 1.2.0
 */
public class GradeDataUtil {

    public static AssessmentGradeSummary getPointBasedAssessmentStarData(PointGradeValues gradeInfo) {
        int STAR_LEVEL_COUNT = 3;
        // if student is not assessed
        if (gradeInfo.getCurrentGrade() == -1) {
            return new AssessmentGradeSummary(0, STAR_LEVEL_COUNT);
        } else {
            double scaledScore = STAR_LEVEL_COUNT * (double) (gradeInfo.getCurrentGrade() - gradeInfo.getMinGrade()) /
                    (gradeInfo.getMaxGrade() - gradeInfo.getMinGrade());
           /*
           we use ceil method because if scaled score is in a range 0 - 1 it should return 1 as star level,
           if it is in range 1 - 2 star level should be 2 and so on.

           Special case is when scaled score is 0 and in this case, 1 should be returned as a star level
            */
           return new AssessmentGradeSummary(scaledScore == 0 ? 1 : (int) Math.ceil(scaledScore), STAR_LEVEL_COUNT);
        }
    }
}
