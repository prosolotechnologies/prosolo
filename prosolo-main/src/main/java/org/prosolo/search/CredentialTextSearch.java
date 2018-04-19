package org.prosolo.search;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.credential.CredentialData;

import java.util.List;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface CredentialTextSearch extends AbstractManager {
	
	/**
	 * @param organizationId
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param userId
	 * @param unitIds
	 * @param filter
	 * @param sortOption
	 * @return
	 */
	PaginatedResult<CredentialData> searchCredentialsForUser(
			long organizationId, String searchTerm, int page, int limit, long userId,
			List<Long> unitIds, CredentialSearchFilterUser filter, LearningResourceSortOption sortOption);
	
	PaginatedResult<CredentialData> searchCredentialsForManager(
			long organizationId, String searchTerm, int page, int limit, long userId,
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption);

	PaginatedResult<CredentialData> searchCredentialsForAdmin(
			long organizationId, long unitId, String searchTerm, int page, int limit,
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption);

}
