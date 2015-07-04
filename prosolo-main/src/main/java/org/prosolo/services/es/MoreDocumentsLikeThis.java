package org.prosolo.services.es;

import java.util.List;

import org.prosolo.recommendation.impl.RecommendedDocument;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * zoran
 */

public interface MoreDocumentsLikeThis {
	
	List<RecommendedDocument> getSuggestedDocumentsForLearningGoal(String likeText, long userId, int limit) throws IndexingServiceNotAvailable;
	
	List<String> findDocumentDuplicates(String likeText) throws IndexingServiceNotAvailable;
	
}
