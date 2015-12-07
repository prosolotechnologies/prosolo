package org.prosolo.recommendation.impl;

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.web.home.SuggestedLearningBean;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service("org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater")
public class LearningGoalRecommendationCacheUpdaterImpl extends AbstractManagerImpl implements LearningGoalRecommendationCacheUpdater, Serializable {
	
	private static final long serialVersionUID = 6663140731583807068L;

	@Inject 
	private SuggestedLearningQueries suggestedLearningQueries;
	
	/* (non-Javadoc)
	 * @see org.prosolo.recommendation.LearningGoalRecommendationCacheUpdater#updateLearningGoalRecommendations(long, javax.servlet.http.HttpSession, org.hibernate.Session)
	 *
	 * @version 0.5
	 */
	@Override
	public void removeLearningGoalRecommendation(long userId, long learningGoalId, HttpSession userSession, Session session) {
		
		if (userSession != null) {
			SuggestedLearningBean suggestedLearningBean = (SuggestedLearningBean) userSession.getAttribute("suggestedLearningBean");

			if (suggestedLearningBean == null) {
				return;
			}
			
			List<Recommendation> recommendations = suggestedLearningQueries.findSuggestedLearningResourcesForResource(userId, learningGoalId);
			ListIterator<Recommendation> recommendationIter = recommendations.listIterator();
			
			while (recommendationIter.hasNext()) {
				Recommendation recommendation = recommendationIter.next();
				recommendation.setDismissed(true);
				session.saveOrUpdate(recommendation);
				
				suggestedLearningBean.removeSuggestedResource(RecommendationType.USER, recommendation.getRecommendedResource().getId());
			}
		} 
	}
}
