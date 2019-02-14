package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.studentprofile.Grade;
import org.prosolo.services.user.data.profile.grade.*;

/**
 * Maps GradeData to Grade domain object
 *
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public class GradeDataToGradeVisitor implements GradeDataVisitor<Grade> {

    @Override
    public Grade visit(NoGradeData grade) {
        return Grade.createNoGrade();
    }

    @Override
    public Grade visit(PointBasedGradeData grade) {
        return Grade.createSimplePointGrade(grade.getPointsAchieved(), grade.getMaxPoints());
    }

    @Override
    public Grade visit(RubricGradeData grade) {
        return Grade.createDescriptiveRubricGrade(grade.getAvgLevelAchieved(), grade.getNumberOfLevels());
    }

    @Override
    public Grade visit(PointRubricGradeData grade) {
        return Grade.createPointRubricGrade(grade.getPointsAchieved(), grade.getMaxPoints(), grade.getAvgLevelAchieved(), grade.getNumberOfLevels());
    }
}
