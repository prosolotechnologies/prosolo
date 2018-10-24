package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.domainmodel.rubric.RubricType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-18
 * @since 1.2.0
 */
public abstract class RubricCriteriaGradeData<T extends RubricCriterionGradeData> implements Serializable {

    private List<T> criteria;

    public RubricCriteriaGradeData(List<T> criteria) {
        this.criteria = new ArrayList<>(criteria);
    }

    public List<T> getCriteria() {
        return criteria;
    }

    public abstract RubricType getRubricType();
}
