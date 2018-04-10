package org.prosolo.web.rubrics.data;

import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;

/**
 * @author stefanvuckovic
 * @date 2018-01-15
 * @since 1.2.0
 */
public enum LabeledRubricType {
    DESCRIPTIVE(RubricType.DESCRIPTIVE, "Descriptive"),
    POINT(RubricType.POINT, "Percentage");
    //POINT_RANGE(RubricType.POINT_RANGE, "Percentage range");

    private RubricType rubricType;
    private String label;

    LabeledRubricType(RubricType rubricType, String label) {
        this.rubricType = rubricType;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public RubricType getRubricType() {
        return rubricType;
    }
}
