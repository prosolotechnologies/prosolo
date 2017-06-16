package org.prosolo.recommendation;

import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
public interface LearningGoalRecommendationCacheUpdater {
	
	/**
	 * Updates session cache with recommendations for a learning goal. This
	 * method is usually invoked when a new learning goal is added/joined.
	 * 
	 * @param userId
	 * @param learningGoalId
	 * @param userSession
	 * @param session
	 * @throws ResourceCouldNotBeLoadedException
	 *			
	 * @version 0.5
	 * @param learningGoalId
	 */
	void removeLearningGoalRecommendation(long userId, long learningGoalId, HttpSession userSession, Session session);
}
