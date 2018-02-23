package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.domainmodel.credential.ActivityRubricVisibility;
import org.prosolo.common.domainmodel.rubric.RubricType;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * This method returns a map with values criterionId : levelId, namely for each criterion the level that was
     * chosen when the resource was graded.
     *
     * @return map with values criterionId : levelId
     */
    public Map<Long, Long> getRubricGrade() {
        Map<Long, Long> gradeMap = new HashMap<>();

        for (RubricCriterionGradeData rubricCriterionGradeData : getRubricCriteria().getCriteria()) {
            gradeMap.put(rubricCriterionGradeData.getId(), rubricCriterionGradeData.getLevelId());
        }

        return gradeMap;
    }

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
