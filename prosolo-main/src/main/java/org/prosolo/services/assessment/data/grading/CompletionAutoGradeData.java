package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class CompletionAutoGradeData extends AutomaticGradeData {

    private static final long serialVersionUID = 568178243078077962L;

    public CompletionAutoGradeData() {}

    public CompletionAutoGradeData(int maxPoints, int grade) {
        super(maxPoints, grade);
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        super.accept(visitor);
        return visitor.visit(this);
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.AUTOMATIC_BY_COMPLETION;
    }
}
