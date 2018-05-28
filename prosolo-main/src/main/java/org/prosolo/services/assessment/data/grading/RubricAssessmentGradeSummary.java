package org.prosolo.services.assessment.data.grading;

/**
 * @author stefanvuckovic
 * @date 2018-05-14
 * @since 1.2.0
 */
public class RubricAssessmentGradeSummary extends AssessmentGradeSummary {

    private String gradeLevelTitle;

    public RubricAssessmentGradeSummary(int grade, int outOf, String gradeLevelTitle) {
        super(grade, outOf);
        this.gradeLevelTitle = gradeLevelTitle;
    }

    public static RubricAssessmentGradeSummary empty() {
        return new RubricAssessmentGradeSummary(0, 0, null);
    }

    public String getGradeLevelTitle() {
        return gradeLevelTitle;
    }
}
