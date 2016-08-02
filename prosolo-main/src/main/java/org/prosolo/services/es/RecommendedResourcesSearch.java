package org.prosolo.services.es;

import java.util.List;

import org.prosolo.common.domainmodel.user.User;

/**
@author Zoran Jeremic Jun 6, 2015
 *
 */

public interface RecommendedResourcesSearch {

	List<User> findMostActiveRecommendedUsers(Long userId,
			List<Long> ignoredUsers, List<Long> userGoalsIds, int limit);

	List<User> findSimilarUsers(Long userId, List<Long> ignoredUsers,  int from, int limit);
}

