package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public class PointRubricGradeData extends RubricGradeData implements PointGradeData {

    private PointBasedGradeData pointBasedGrade;

    public PointRubricGradeData(int avgLevelAchieved, int numberOfLevels, int pointsAchieved, int maxPoints) {
        super(avgLevelAchieved, numberOfLevels);
        this.pointBasedGrade = new PointBasedGradeData(pointsAchieved, maxPoints);
    }

    @Override
    public int getPointsAchieved() {
        return pointBasedGrade.getPointsAchieved();
    }

    @Override
    public void setPointsAchieved(int pointsAchieved) {
        pointBasedGrade.setPointsAchieved(pointsAchieved);
    }

    @Override
    public int getMaxPoints() {
        return pointBasedGrade.getMaxPoints();
    }

    @Override
    public void setMaxPoints(int maxPoints) {
        pointBasedGrade.setMaxPoints(maxPoints);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " (" + pointBasedGrade.getDescription() + ")";
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
