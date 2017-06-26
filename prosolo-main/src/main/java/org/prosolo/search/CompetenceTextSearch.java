package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.CompetenceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.web.search.data.SortingOption;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface CompetenceTextSearch extends AbstractManager {
	
	/**
	 * 
	 * @param userId
	 * @param searchString
	 * @param page
	 * @param limit
	 * @param loadOneMore
	 * @param toExclude
	 * @param sortTitleAsc
	 * @return
	 */
	PaginatedResult<CompetenceData1> searchCompetencesForAddingToCredential(long userId, String searchString, int page, int limit,
                                                                            boolean loadOneMore, long[] toExclude, SortingOption sortTitleAsc);
	
	PaginatedResult<CompetenceData1> searchCompetences(
			String searchTerm, int page, int limit, long userId, 
			LearningResourceSearchFilter filter, LearningResourceSortOption sortOption, 
			CompetenceSearchConfig config);
	
	PaginatedResult<CompetenceData1> searchCompetencesForManager(
			String searchTerm, int page, int limit, long userId, CompetenceSearchFilter filter, 
			LearningResourceSortOption sortOption);

}
