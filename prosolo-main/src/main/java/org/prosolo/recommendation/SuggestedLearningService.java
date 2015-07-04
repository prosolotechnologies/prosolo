package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.domainmodel.activities.Recommendation;
import org.prosolo.domainmodel.activities.RecommendationType;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.User;

public interface SuggestedLearningService {
	
	List<Recommendation> findSuggestedLearningResourcesByCollegues(User currentUser, RecommendationType recType,
			int page, int limit);

	List<Node> findSuggestedLearningResourcesBySystem(User user, int limit);
 
	int findNumberOfSuggestedLearningResourcesByCollegues(User user,
			RecommendationType recType);

	List<Node> findSuggestedLearningResourcesByCourse(User user,
			int defaultLikeThisItemsNumber);
 
}
