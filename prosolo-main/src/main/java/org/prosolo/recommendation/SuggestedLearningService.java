package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

public interface SuggestedLearningService {
	
	List<Recommendation> findSuggestedLearningResourcesByCollegues(long userId, RecommendationType recType,
			int page, int limit);

	List<Node> findSuggestedLearningResourcesBySystem(long userId, int limit) throws ResourceCouldNotBeLoadedException;
 
	int findNumberOfSuggestedLearningResourcesByCollegues(long userId,
			RecommendationType recType);

	List<Node> findSuggestedLearningResourcesByCourse(long userId,
			int defaultLikeThisItemsNumber);
 
}
