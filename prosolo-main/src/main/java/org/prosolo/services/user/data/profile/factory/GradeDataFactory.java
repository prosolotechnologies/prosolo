package org.prosolo.services.user.data.profile.factory;


import org.prosolo.common.domainmodel.studentprofile.Grade;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.assessment.data.grading.GradeDataUtil;
import org.prosolo.services.assessment.data.grading.PointGradeValues;
import org.prosolo.services.user.data.profile.grade.*;
import org.springframework.stereotype.Component;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
@Component
public class GradeDataFactory {

    public Grade getGradeFromGradeData(GradeData gradeData) {
        return gradeData.accept(new GradeDataToGradeVisitor());
    }

    public GradeData getGradeDataFromGrade(Grade grade) {
        switch (grade.getGradeType()) {
            case NO_GRADE:
                return new NoGradeData();
            case SIMPLE_POINT:
                return getPointBasedGradeData(grade.getPointsAchieved(), grade.getMaxPoints());
            case DESC_RUBRIC:
                return new RubricGradeData(grade.getAvgLevelAchieved(), grade.getNumberOfLevels());
            case POINT_RUBRIC:
                return new PointRubricGradeData(grade.getAvgLevelAchieved(), grade.getNumberOfLevels(), grade.getPointsAchieved(), grade.getMaxPoints());
            default:
                return null;
        }
    }

    public PointBasedGradeData getPointBasedGradeData(int pointsAchieved, int maxPoints) {
        AssessmentGradeSummary assessmentGradeSummary = GradeDataUtil.getPointBasedAssessmentStarData(new PointGradeValues(0, maxPoints, pointsAchieved));
        return new PointBasedGradeData(pointsAchieved, maxPoints, assessmentGradeSummary.getGrade(), assessmentGradeSummary.getOutOf());
    }
}
