package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;

public interface CompetenceRecommendation {

	List<Competence> recommendCompetences(User user, TargetLearningGoal tGoal, int limit);
}
