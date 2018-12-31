package org.prosolo.common.domainmodel.studentprofile;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
@Embeddable
public class Grade implements Serializable {

    private static final long serialVersionUID = 7046309203407893626L;

    private GradeType gradeType;
    private int pointsAchieved;
    private int maxPoints;
    private int avgLevelAchieved;
    private int numberOfLevels;

    public Grade() {}

    private Grade(GradeType gradeType, int pointsAchieved, int maxPoints, int avgLevelAchieved, int numberOfLevels) {
        this.gradeType = gradeType;
        this.pointsAchieved = pointsAchieved;
        this.maxPoints = maxPoints;
        this.avgLevelAchieved = avgLevelAchieved;
        this.numberOfLevels = numberOfLevels;
    }

    public static Grade createSimplePointGrade(int pointsAchieved, int maxPoints) {
        return new Grade(GradeType.SIMPLE_POINT, pointsAchieved, maxPoints, -1, -1);
    }

    public static Grade createPointRubricGrade(int pointsAchieved, int maxPoints, int avgLevelAchieved, int numberOfLevels) {
        return new Grade(GradeType.POINT_RUBRIC, pointsAchieved, maxPoints, avgLevelAchieved, numberOfLevels);
    }

    public static Grade createDescriptiveRubricGrade(int avgLevelAchieved, int numberOfLevels) {
        return new Grade(GradeType.POINT_RUBRIC, -1, -1, avgLevelAchieved, numberOfLevels);
    }

    public static Grade createNoGrade() {
        return new Grade(GradeType.NO_GRADE, -1, -1, -1, -1);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public GradeType getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeType gradeType) {
        this.gradeType = gradeType;
    }

    public int getPointsAchieved() {
        return pointsAchieved;
    }

    public void setPointsAchieved(int pointsAchieved) {
        this.pointsAchieved = pointsAchieved;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public int getAvgLevelAchieved() {
        return avgLevelAchieved;
    }

    public void setAvgLevelAchieved(int avgLevelAchieved) {
        this.avgLevelAchieved = avgLevelAchieved;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }
}
