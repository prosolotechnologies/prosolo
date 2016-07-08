package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.recommendation.impl.RecommendedDocument;

public interface DocumentsRecommendation {
	
	List<RecommendedDocument> recommendDocuments(long userId, TargetLearningGoal tGoal, int limit);
	
}
