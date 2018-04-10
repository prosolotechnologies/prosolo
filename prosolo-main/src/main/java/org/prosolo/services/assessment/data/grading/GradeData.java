package org.prosolo.services.assessment.data.grading;

import org.prosolo.common.util.Pair;

public interface GradeData {

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

	default boolean isAssessed() {
		return getCurrentGrade() >= 0;
	}

	GradingMode getGradingMode();
	boolean isPointBasedGrading();

	default Pair<Integer, Integer> getAssessmentStarData() {
		return null;
	}
}
