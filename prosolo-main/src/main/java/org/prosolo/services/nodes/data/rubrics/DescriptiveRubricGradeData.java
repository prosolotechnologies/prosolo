package org.prosolo.services.nodes.data.rubrics;

import org.prosolo.common.domainmodel.rubric.RubricType;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class DescriptiveRubricGradeData extends RubricGradeData<ActivityRubricCriterionData> {

    public DescriptiveRubricGradeData(List<ActivityRubricCriterionData> criteria) {
        super(criteria);
    }

//    @Override
//    public int getGivenGrade() {
//        return -1;
//    }


    @Override
    public RubricType getRubricType() {
        return RubricType.DESCRIPTIVE;
    }
}
