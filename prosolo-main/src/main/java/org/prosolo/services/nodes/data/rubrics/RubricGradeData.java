package org.prosolo.services.nodes.data.rubrics;

import com.jcabi.immutable.Array;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.services.nodes.data.assessments.GradeData;
import org.prosolo.services.nodes.data.assessments.GradingMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public abstract class RubricGradeData<T extends ActivityRubricCriterionData> extends GradeData {

    private List<T> criteria;

    public RubricGradeData(List<T> criteria) {
        this.criteria = new ArrayList<>(criteria);
    }

    public List<T> getCriteria() {
        return criteria;
    }

    @Override
    public GradingMode getGradingMode() {
        return GradingMode.MANUAL_RUBRIC;
    }

    @Override
    public boolean isAssessed() {
        //we know that student is assessed when there is a level selected for one criterion
        return criteria.get(0).getLevelId() > 0;
    }

    public abstract RubricType getRubricType();
}
