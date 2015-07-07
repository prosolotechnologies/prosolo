package org.prosolo.services.es;

import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 *
 * @author Zoran Jeremic, Aug 16, 2014
 *
 */
public interface MoreUsersLikeThis {

	List<User> getRecommendedCollaboratorsForLearningGoal(String likeText,
			Collection<User> ignoredUsers, int limit);

	List<User> getRecommendedCollaboratorsBasedOnLocation(String likeText, Collection<User> ignoredUsers,
			double lat, double lon, int limit);

	List<User> getRecommendedCollaboratorsBasedOnSimilarity(String likeText,
			Collection<User> ignoredUsers, int limit) throws IndexingServiceNotAvailable;

	List<User> getCollaboratorsBasedOnLocation(Collection<User> ignoredUsers,
			double lat, double lon, int limit);

}
