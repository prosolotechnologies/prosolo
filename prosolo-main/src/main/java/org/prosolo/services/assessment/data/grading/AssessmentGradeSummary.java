package org.prosolo.services.assessment.data.grading;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-05-14
 * @since 1.2.0
 */
public class AssessmentGradeSummary implements Serializable {

    private static final long serialVersionUID = 208070635727178541L;

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
