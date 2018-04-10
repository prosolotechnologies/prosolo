package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-17
 * @since 1.2.0
 */
public interface GradeDataVisitor<T> {

    T visit(ManualSimpleGradeData gradeData);
    T visit(AutomaticGradeData gradeData);
    T visit(ExternalToolAutoGradeData gradeData);
    T visit(CompletionAutoGradeData gradeData);
    T visit(NongradedGradeData gradeData);
    T visit(RubricGradeData gradeData);
    T visit(DescriptiveRubricGradeData gradeData);
    T visit(PointRubricGradeData gradeData);
}
