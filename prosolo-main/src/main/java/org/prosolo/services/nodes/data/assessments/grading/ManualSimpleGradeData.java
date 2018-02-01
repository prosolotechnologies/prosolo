package org.prosolo.services.nodes.data.assessments.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class ManualSimpleGradeData implements GradeData {

    private PointGradeValues gradeInfo;
    private int newGrade;

    public ManualSimpleGradeData() {}

    public ManualSimpleGradeData(int currentGrade, int maxPoints) {
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
    public void updateCurrentGrade(int grade) {
        gradeInfo.setCurrentGrade(grade);
    }

    @Override
    public int calculateGrade() {
        //set new grade as current
        gradeInfo.setCurrentGrade(newGrade);
        return newGrade;
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.MANUAL_SIMPLE;
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

    public int getNewGrade() {
        return newGrade;
    }

    public void setNewGrade(int newGrade) {
        this.newGrade = newGrade;
    }

    public int getMinGrade() {
        return gradeInfo.getMinGrade();
    }
}
