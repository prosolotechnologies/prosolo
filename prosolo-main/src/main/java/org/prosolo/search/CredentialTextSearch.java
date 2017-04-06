package org.prosolo.search;

import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
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
	 * Returns credentials that user with id specified by {@code userId} is allowed to see.
	 * 
	 * Conditions that should be met in order for credential to be returned:
	 *  - credential is published and visible to all users or
	 *  - credential is published and user has View privilege or
	 *  - user is enrolled in a credential (currently learning or completed credential) or
	 *  - user is owner of a credential or
	 *  - user has Edit privilege for credential
	 *  
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param userId
	 * @param filter
	 * @param sortOption
	 * @param includeEnrolledCredentials
	 * @return
	 */
	TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			LearningResourceSearchFilter filter, LearningResourceSortOption sortOption, 
			boolean includeEnrolledCredentials, boolean includeCredentialsWithViewPrivilege);
	
	TextSearchResponse1<CredentialData> searchCredentialsForManager(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption);

}
