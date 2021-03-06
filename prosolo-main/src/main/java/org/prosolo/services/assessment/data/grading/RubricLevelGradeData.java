package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2017-10-16
 * @since 1.0.0
 */
public class RubricLevelGradeData extends RubricItemGradeData {

    private static final long serialVersionUID = 82789843715531990L;

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
