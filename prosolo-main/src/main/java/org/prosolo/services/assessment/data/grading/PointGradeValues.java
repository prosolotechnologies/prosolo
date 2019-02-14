package org.prosolo.services.assessment.data.grading;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-01-17
 * @since 1.2.0
 */
public class PointGradeValues implements Serializable {

    private static final long serialVersionUID = 3228805388475134878L;

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
