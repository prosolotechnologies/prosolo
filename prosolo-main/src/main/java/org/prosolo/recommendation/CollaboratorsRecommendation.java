package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;

public interface CollaboratorsRecommendation {
	
	List<User> getRecommendedCollaboratorsForLearningGoal(User loggedUser, long targetGoalId, int defaultLikeThisItemsNumber);
	
	List<User> getRecommendedCollaboratorsBasedOnLocation(User loggedUser, int limit);
	
	List<User> getRecommendedCollaboratorsBasedOnSimilarity(User loggedUser, int limit);
	
	List<User> getMostActiveRecommendedUsers(User loggedUser, int limit);
	
	void initializeMostActiveRecommendedUsers();
	
}
