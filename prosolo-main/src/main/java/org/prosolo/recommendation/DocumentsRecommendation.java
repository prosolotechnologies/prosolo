package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.recommendation.impl.RecommendedDocument;
 

/**
zoran
 */

public interface DocumentsRecommendation {

	//List<RecommendedDocument> recommendDocuments(User user, LearningGoal goal, int limit);

	List<RecommendedDocument> recommendDocuments(User user,
			TargetLearningGoal tGoal, int limit);

}
