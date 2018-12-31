package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public interface GradeDataVisitor<T> {

    T visit(NoGradeData grade);
    T visit(PointBasedGradeData grade);
    T visit(RubricGradeData grade);
    T visit(PointRubricGradeData grade);
}
