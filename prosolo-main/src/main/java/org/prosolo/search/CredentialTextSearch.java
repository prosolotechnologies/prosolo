package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CredentialData;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface CredentialTextSearch extends AbstractManager {
	
	/**
	 * 
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param userId
	 * @param filter
	 * @param sortOption
	 * @return
	 */
	PaginatedResult<CredentialData> searchCredentialsForUser(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterUser filter, LearningResourceSortOption sortOption);
	
	PaginatedResult<CredentialData> searchCredentialsForManager(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption);

}
