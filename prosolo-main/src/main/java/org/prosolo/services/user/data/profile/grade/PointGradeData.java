package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public interface PointGradeData extends GradeData {

    public int getPointsAchieved();

    public void setPointsAchieved(int pointsAchieved);

    public int getMaxPoints();

    public void setMaxPoints(int maxPoints);
}
