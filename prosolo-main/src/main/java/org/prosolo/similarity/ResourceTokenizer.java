package org.prosolo.similarity;

import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;

public interface ResourceTokenizer {
	
	String getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal);
	
	void getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal, StringBuffer userTokensBuffer);
	
	String getTokenizedStringForUser(User user);

	String getTokenizedStringForCourse(Course course);
	
	String getTokenizedStringForTargetCompetence(TargetCompetence tCompetence);
	
}
