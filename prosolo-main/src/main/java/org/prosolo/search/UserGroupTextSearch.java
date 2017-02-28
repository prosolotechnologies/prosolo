package org.prosolo.search;

import java.util.List;

import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.UserGroupData;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface UserGroupTextSearch extends AbstractManager {

	TextSearchResponse1<UserGroupData> searchUserGroups (
			String searchString, int page, int limit);
	
	TextSearchResponse1<UserGroupData> searchUserGroupsForUser (
			String searchString, long userId, int page, int limit);
	
	/**
	 * Returns combined top {@code limit} users and groups that are not currently assigned to
	 * credential given by {@code credId}
	 * @param credId
	 * @param searchTerm
	 * @param limit
	 * @param usersToExclude
	 * @return
	 */
	TextSearchResponse1<ResourceVisibilityMember> searchCredentialUsersAndGroups(long credId,
			String searchTerm, int limit, List<Long> usersToExclude, List<Long> groupsToExclude);
	
	TextSearchResponse1<ResourceVisibilityMember> searchVisibilityUsers(String searchTerm,
			int limit, List<Long> usersToExclude);
	
	TextSearchResponse1<ResourceVisibilityMember> searchCompetenceUsersAndGroups(long compId,
			String searchTerm, int limit, List<Long> usersToExclude, List<Long> groupsToExclude);

}
