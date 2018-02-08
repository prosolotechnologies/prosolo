package org.prosolo.services.nodes.data.assessments.grading;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.rubric.RubricType;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class DescriptiveRubricGradeData extends RubricGradeData<DescriptiveRubricCriteriaGradeData> {

    //it will be -1 or 0 depending on whether student is assessed or not
    private int currentGrade;

    public DescriptiveRubricGradeData() {}

    public DescriptiveRubricGradeData(ActivityRubricVisibility rubricVisibilityForStudent, int currentGrade) {
        super(rubricVisibilityForStudent);
        this.currentGrade = currentGrade;
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        super.accept(visitor);
        return visitor.visit(this);
    }

    @Override
    public int getCurrentGrade() {
        return currentGrade;
    }

    @Override
    public int calculateGrade() {
        if (!isInitialized()) {
            throw new IllegalStateException("Rubric grade data is not fully initialized so grade can't be calculated");
        }
        int newGrade;
        //if level is not selected for any of the criteria, grade can not be given so negative value is returned
        if (getRubricCriteria().getCriteria().get(0).getLevelId() == 0) {
            newGrade = -1;
        } else {
            newGrade = 0;
        }

        //set new grade as current
        currentGrade = newGrade;
        return newGrade;
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
    public RubricType getRubricType() {
        return RubricType.DESCRIPTIVE;
    }

    public void setCurrentGrade(int currentGrade) {
        this.currentGrade = currentGrade;
    }
}
