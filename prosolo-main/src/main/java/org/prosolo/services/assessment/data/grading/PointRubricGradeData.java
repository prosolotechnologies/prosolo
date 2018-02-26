package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.rubric.RubricType;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class PointRubricGradeData extends RubricGradeData<PointRubricCriteriaGradeData> {

    private PointGradeValues gradeInfo;

    public PointRubricGradeData() {}

    public PointRubricGradeData(ActivityRubricVisibility rubricVisibilityForStudent, int maxPoints, int currentGrade) {
        super(rubricVisibilityForStudent);
        this.gradeInfo = new PointGradeValues(0, maxPoints, currentGrade);
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        super.accept(visitor);
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
    public int calculateGrade() throws IllegalStateException {
        if (!isInitialized()) {
            throw new IllegalStateException("Rubric grade data is not fully initialized so grade can't be calculated");
        }
        int newGrade;
        //if level is not selected for any of the criteria, grade can not be given so negative value is returned
        if (getRubricCriteria().getCriteria().get(0).getLevelId() == 0) {
            newGrade = -1;
        } else {
            newGrade = getRubricCriteria().getCriteria()
                    .stream()
                    .mapToInt(c -> c.getLevels().stream()
                            .filter(lvl -> lvl.getId() == c.getLevelId())
                            .findFirst().get().getPoints()).sum();
        }
        //set new grade as current
        gradeInfo.setCurrentGrade(newGrade);
        return newGrade;
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
    public RubricType getRubricType() {
        return RubricType.POINT;
    }

    public void setGradeInfo(int maxPoints, int currentGrade) {
        this.gradeInfo = new PointGradeValues(0, maxPoints, currentGrade);
    }
}
