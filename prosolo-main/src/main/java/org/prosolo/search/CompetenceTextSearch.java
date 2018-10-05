package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.CompetenceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.web.search.data.SortingOption;

import java.util.List;

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
	PaginatedResult<CompetenceData1> searchCompetencesForAddingToCredential(long organizationId, long userId,
																			String searchString, int page, int limit,
																			boolean loadOneMore, List<Long> unitIds,
																			long[] toExclude, SortingOption sortTitleAsc);
	
	PaginatedResult<CompetenceData1> searchCompetences(
			long organizationId, String searchTerm, int page, int limit, long userId,
			List<Long> unitIds, LearningResourceSearchFilter filter, CompetenceSearchConfig config);
	
	PaginatedResult<CompetenceData1> searchCompetencesForManager(
			long organizationId, String searchTerm, int page, int limit, long userId, CompetenceSearchFilter filter);

}
