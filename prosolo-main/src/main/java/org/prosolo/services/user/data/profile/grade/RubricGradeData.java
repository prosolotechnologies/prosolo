package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public class RubricGradeData implements GradeData {

    private int avgLevelAchieved;
    private int numberOfLevels;

    public RubricGradeData(int avgLevelAchieved, int numberOfLevels) {
        this.avgLevelAchieved = avgLevelAchieved;
        this.numberOfLevels = numberOfLevels;
    }

    public int getAvgLevelAchieved() {
        return avgLevelAchieved;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    @Override
    public String getDescription() {
        return "Level " + avgLevelAchieved + "/" + numberOfLevels;
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int getScaledGrade() {
        return getAvgLevelAchieved();
    }

    @Override
    public int getScaledMaxGrade() {
        return getNumberOfLevels();
    }
}
