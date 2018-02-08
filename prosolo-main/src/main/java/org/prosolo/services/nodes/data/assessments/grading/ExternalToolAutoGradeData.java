package org.prosolo.services.nodes.data.assessments.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class ExternalToolAutoGradeData extends AutomaticGradeData {

    public ExternalToolAutoGradeData() {}

    public ExternalToolAutoGradeData(int maxPoints, int grade) {
        super(maxPoints, grade);
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        super.accept(visitor);
        return visitor.visit(this);
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.AUTOMATIC_BY_EXTERNAL_TOOL;
    }
}
