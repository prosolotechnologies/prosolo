package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class AutomaticGradeData implements GradeData {

    private static final long serialVersionUID = 7237613666252186916L;

    private PointGradeValues gradeInfo;
    private AssessmentGradeSummary assessmentStarData;

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
    public void updateCurrentGrade(int grade) {
        gradeInfo.setCurrentGrade(grade);
    }

    @Override
    public int calculateGrade() {
        return gradeInfo.getCurrentGrade();
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.AUTOMATIC;
    }

    @Override
    public boolean isPointBasedGrading() {
        return true;
    }

    @Override
    public int getMaxGrade() {
        return gradeInfo.getMaxGrade();
    }

    @Override
    public int getMinGrade() {
        return gradeInfo.getMinGrade();
    }

    public void setGradeInfo(int maxPoints, int currentGrade) {
        this.gradeInfo = new PointGradeValues(0, maxPoints, currentGrade);
    }

    public void calculateAssessmentStarData() {
        assessmentStarData = GradeDataUtil.getPointBasedAssessmentStarData(gradeInfo);
    }

    @Override
    public AssessmentGradeSummary getAssessmentStarData() {
        return assessmentStarData;
    }

}
