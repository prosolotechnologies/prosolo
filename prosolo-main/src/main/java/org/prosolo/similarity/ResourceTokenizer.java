package org.prosolo.similarity;

import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;

public interface ResourceTokenizer {
	
	String getTokenizedStringForUserLearningGoal(TargetLearningGoal tGoal);
	
	void getTokenizedStringForUserLearningGoal(TargetLearningGoal tGoal, StringBuffer userTokensBuffer);
	
	String getTokenizedStringForUser(User user);

	String getTokenizedStringForCourse(Course course);
	
	String getTokenizedStringForTargetCompetence(TargetCompetence tCompetence);
	
}
