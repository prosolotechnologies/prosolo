package org.prosolo.search;

import org.prosolo.search.impl.TextSearchResponse1;
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
	TextSearchResponse1<CredentialData> searchCredentialsForUser(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterUser filter, LearningResourceSortOption sortOption);
	
	TextSearchResponse1<CredentialData> searchCredentialsForManager(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption);

}
