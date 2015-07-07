package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.recommendation.DocumentsRecommendation;
import org.prosolo.services.es.MoreDocumentsLikeThis;
import org.prosolo.similarity.ResourceTokenizer;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
zoran
 */
@Service("org.prosolo.recommendation.DocumentsRecommendation")
public class DocumentsRecommendationImpl implements DocumentsRecommendation {
	
	private Logger logger = Logger.getLogger(DocumentsRecommendationImpl.class);
	
	@Autowired private MoreDocumentsLikeThis mdlt;
	@Autowired private ResourceTokenizer resTokenizer;
 
	@Override
	public List<RecommendedDocument> recommendDocuments(User user, TargetLearningGoal tGoal, int limit) {
		String tokenizedString = resTokenizer.getTokenizedStringForUserLearningGoal(user, tGoal);
		
		try {
			return mdlt.getSuggestedDocumentsForLearningGoal(tokenizedString, user.getId(), limit);
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
			return new ArrayList<RecommendedDocument>();
		}
	}
}
