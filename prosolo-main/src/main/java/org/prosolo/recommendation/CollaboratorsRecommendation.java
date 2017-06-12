package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
public interface CollaboratorsRecommendation {
	
//	List<User> getRecommendedCollaboratorsForLearningGoal(long userId, long targetGoalId, int defaultLikeThisItemsNumber);
	
	List<User> getRecommendedCollaboratorsBasedOnLocation(long userId, int limit) throws ResourceCouldNotBeLoadedException;
	
	List<User> getRecommendedCollaboratorsBasedOnSimilarity(long userId, int limit) throws ResourceCouldNotBeLoadedException;
	
	List<User> getMostActiveRecommendedUsers(long userId, int limit);
	
	//void initializeMostActiveRecommendedUsers();
	
}
