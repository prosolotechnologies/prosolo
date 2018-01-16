package org.prosolo.services.nodes.data.assessments;

/**
 * @author stefanvuckovic
 * @date 2018-01-11
 * @since 1.2.0
 */
public abstract class AutomaticGradeData extends GradeData {

    private int grade;
    private boolean assessed;

    public AutomaticGradeData(int grade, boolean assessed) {
        this.grade = grade;
        this.assessed = assessed;
    }

//    @Override
//    public int getGivenGrade() {
//        return grade;
//    }
//
//    @Override
//    public boolean isAssessed() {
//        return assessed;
//    }

}
