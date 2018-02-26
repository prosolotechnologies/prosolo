package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public class PointRubricCriterionGradeData extends RubricCriterionGradeData<PointRubricLevelGradeData> {

    private double weight;
    private int points;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
