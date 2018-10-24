package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class NongradedGradeData implements GradeData {

    private static final long serialVersionUID = -4123758661020690387L;

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int getCurrentGrade() {
        return -1;
    }

    @Override
    public void updateCurrentGrade(int grade) {}

    @Override
    public int calculateGrade() {
        return -1;
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.NONGRADED;
    }

    @Override
    public boolean isPointBasedGrading() {
        return false;
    }

    @Override
    public int getMaxGrade() {
        return 0;
    }

    @Override
    public int getMinGrade() {
        return 0;
    }

}
