package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public interface GradeData {

    String getDescription();
    int getScaledGrade();
    int getScaledMaxGrade();

    <T> T accept(GradeDataVisitor<T> visitor);
}
