package org.prosolo.services.user.data.profile.grade;

/**
 * @author stefanvuckovic
 * @date 2018-12-31
 * @since 1.2.0
 */
public class NoGradeData implements GradeData {

    @Override
    public String getDescription() {
        return "Nongraded";
    }

    @Override
    public int getScaledGrade() {
        return 0;
    }

    @Override
    public int getScaledMaxGrade() {
        return 0;
    }

    @Override
    public <T> T accept(GradeDataVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
