package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.learningevidence.LearningEvidenceSearchFilter;
import org.prosolo.search.util.learningevidence.LearningEvidenceSortOption;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-12-07
 * @since 1.2.0
 */
public interface LearningEvidenceTextSearch {

    PaginatedResult<LearningEvidenceData> searchLearningEvidences(long orgId, long userId, List<Long> evidencesToExclude,
                                                                  String searchTerm, int page, int limit);

    PaginatedResult<LearningEvidenceData> searchUserLearningEvidences(long orgId, long userId, String searchTerm,
                                                                      int page, int limit, LearningEvidenceSearchFilter filter,
                                                                      LearningEvidenceSortOption sortOption);
}
