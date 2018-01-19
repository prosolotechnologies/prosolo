package org.prosolo.services.nodes.data.assessments.grading;

/**
 * @author stefanvuckovic
 * @date 2018-01-17
 * @since 1.2.0
 */
public class PointGradeValues {

    private final int minGrade;
    private final int maxGrade;
    private int currentGrade;

    public PointGradeValues(int minGrade, int maxGrade, int currentGrade) {
        this.minGrade = minGrade;
        this.maxGrade = maxGrade;
        this.currentGrade = currentGrade;
    }

    public int getMinGrade() {
        return minGrade;
    }

    public int getMaxGrade() {
        return maxGrade;
    }

    public int getCurrentGrade() {
        return currentGrade;
    }

    public void setCurrentGrade(int currentGrade) {
        this.currentGrade = currentGrade;
    }
}
