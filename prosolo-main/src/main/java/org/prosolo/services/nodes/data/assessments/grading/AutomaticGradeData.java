package org.prosolo.services.nodes.data.assessments.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class AutomaticGradeData implements GradeData {

    private PointGradeValues gradeInfo;

    public AutomaticGradeData() {}

    public AutomaticGradeData(int currentGrade) {
        this(0, currentGrade);
    }

    public AutomaticGradeData(int maxPoints, int currentGrade) {
        this.gradeInfo = new PointGradeValues(0, maxPoints, currentGrade);
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int getCurrentGrade() {
        return gradeInfo.getCurrentGrade();
    }

    @Override
    public int calculateGrade() {
        return gradeInfo.getCurrentGrade();
    }

    @Override
    public GradingMode getGradingMode() {
        return null;
    }

    @Override
    public boolean isPointBasedGrading() {
        return true;
    }

    @Override
    public int getMaxGrade() {
        return gradeInfo.getMaxGrade();
    }

    public void setGradeInfo(int maxPoints, int currentGrade) {
        this.gradeInfo = new PointGradeValues(0, maxPoints, currentGrade);
    }

}
