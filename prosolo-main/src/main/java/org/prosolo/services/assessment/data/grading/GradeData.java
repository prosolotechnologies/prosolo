package org.prosolo.services.assessment.data.grading;

import java.io.Serializable;

public interface GradeData extends Serializable {

	<T> T accept(GradeDataVisitor<T> visitor);

	/**
	 * Returns current grade that is set for the assessment
	 *
	 * @return
	 */
	int getCurrentGrade();

	void updateCurrentGrade(int grade);

	/**
	 * Calculates new grade, sets it as current grade and returns it
	 *
	 * @return
	 */
	int calculateGrade();
	int getMaxGrade();
	int getMinGrade();

	default boolean isAssessed() {
		return getCurrentGrade() >= 0;
	}

	GradingMode getGradingMode();
	boolean isPointBasedGrading();

	default AssessmentGradeSummary getAssessmentStarData() {
		return null;
	}
}
