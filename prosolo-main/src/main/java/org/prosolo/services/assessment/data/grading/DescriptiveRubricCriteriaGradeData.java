package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.domainmodel.rubric.RubricType;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-18
 * @since 1.2.0
 */
public class DescriptiveRubricCriteriaGradeData extends RubricCriteriaGradeData<RubricCriterionGradeData> {

    private static final long serialVersionUID = 5083590874934386621L;

    public DescriptiveRubricCriteriaGradeData(List<RubricCriterionGradeData> criteria) {
        super(criteria);
    }

    @Override
    public RubricType getRubricType() {
        return RubricType.DESCRIPTIVE;
    }

}
