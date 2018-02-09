package org.prosolo.services.nodes.data.assessments.grading;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.rubric.RubricType;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public abstract class RubricGradeData<T extends RubricCriteriaGradeData<? extends RubricCriterionGradeData>> implements GradeData {

    private T rubricCriteria;
    private ActivityRubricVisibility rubricVisibilityForStudent;

    protected RubricGradeData() {}

    protected RubricGradeData(ActivityRubricVisibility rubricVisibilityForStudent) {
        this.rubricVisibilityForStudent = rubricVisibilityForStudent;
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public boolean isInitialized() {
        return rubricCriteria != null;
    }

    public T getRubricCriteria() {
        return rubricCriteria;
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.MANUAL_RUBRIC;
    }

    public abstract RubricType getRubricType();

    public void setRubricVisibilityForStudent(ActivityRubricVisibility rubricVisibilityForStudent) {
        this.rubricVisibilityForStudent = rubricVisibilityForStudent;
    }

    public ActivityRubricVisibility getRubricVisibilityForStudent() {
        return rubricVisibilityForStudent;
    }

    public void setRubricCriteria(T rubricCriteria) {
        this.rubricCriteria = rubricCriteria;
    }
}
