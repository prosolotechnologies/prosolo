package org.prosolo.search;

import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.web.search.data.SortingOption;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface CompetenceTextSearch extends AbstractManager {
	
	/**
	 * Returns competences that user with id specified by {@code userId} is allowed to see.
	 * 
	 * Conditions that should be met in order for competence to be returned:
	 *  - competence is published and visible to all users or
	 *  - competence is published and user has View privilege or
	 *  - user is owner of a competence or
	 *  - user has Edit privilege for competence
	 *  
	 * @param userId
	 * @param role
	 * @param searchString
	 * @param page
	 * @param limit
	 * @param loadOneMore
	 * @param toExclude
	 * @param filterTags
	 * @param sortTitleAsc
	 * @return
	 */
	TextSearchResponse1<CompetenceData1> searchCompetences(long userId, Role role,
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc);
	
	TextSearchResponse1<CompetenceData1> searchCompetences(
			String searchTerm, int page, int limit, long userId, 
			LearningResourceSearchFilter filter, LearningResourceSortOption sortOption, 
			LearningResourceSearchConfig config);
	
	TextSearchResponse1<CompetenceData1> searchCompetencesForManager(
			String searchTerm, int page, int limit, long userId, CompetenceSearchFilter filter, 
			LearningResourceSortOption sortOption);

}
