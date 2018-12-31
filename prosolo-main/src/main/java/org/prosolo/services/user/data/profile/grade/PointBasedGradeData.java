package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public class PointBasedGradeData implements PointGradeData {

    private int pointsAchieved;
    private int maxPoints;
    private int scaledPointsAchieved;
    private int scaledMaxPoints;

    public PointBasedGradeData(int pointsAchieved, int maxPoints) {
        this.pointsAchieved = pointsAchieved;
        this.maxPoints = maxPoints;
    }

    public PointBasedGradeData(int pointsAchieved, int maxPoints, int scaledPointsAchieved, int scaledMaxPoints) {
        this.pointsAchieved = pointsAchieved;
        this.maxPoints = maxPoints;
        this.scaledPointsAchieved = scaledPointsAchieved;
        this.scaledMaxPoints = scaledMaxPoints;
    }

    @Override
    public int getPointsAchieved() {
        return pointsAchieved;
    }

    @Override
    public void setPointsAchieved(int pointsAchieved) {
        this.pointsAchieved = pointsAchieved;
    }

    @Override
    public int getMaxPoints() {
        return maxPoints;
    }

    @Override
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public int getScaledMaxPoints() {
        return scaledMaxPoints;
    }

    public int getScaledPointsAchieved() {
        return scaledPointsAchieved;
    }

    @Override
    public String getDescription() {
        return pointsAchieved + "/" + maxPoints + "points";
    }

    @Override
    public int getScaledGrade() {
        return getScaledGrade();
    }

    @Override
    public int getScaledMaxGrade() {
        return getScaledMaxPoints();
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
