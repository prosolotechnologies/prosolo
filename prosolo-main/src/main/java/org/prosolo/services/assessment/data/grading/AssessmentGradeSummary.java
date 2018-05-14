package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-05-14
 * @since 1.2.0
 */
public class AssessmentGradeSummary {

    private final int grade;
    private final int outOf;

    public AssessmentGradeSummary(int grade, int outOf) {
        this.grade = grade;
        this.outOf = outOf;
    }

    public int getGrade() {
        return grade;
    }

    public int getOutOf() {
        return outOf;
    }

}
