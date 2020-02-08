package org.prosolo.search;

import java.util.List;

import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.user.data.UserGroupData;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface UserGroupTextSearch extends AbstractManager {

	PaginatedResult<UserGroupData> searchUserGroups (
			long orgId, long unitId, String searchString, int page, int limit);

	PaginatedResult<BasicObjectInfo> searchUserGroupsAndReturnBasicInfo(long orgId, long unitId, String searchString, int page, int limit);
	
	/**
	 * Returns combined top {@code limit} users and groups that are not currently assigned to
	 * credential given by {@code credId}
	 * @param orgId
	 * @param searchTerm
	 * @param limit
	 * @param usersToExclude
	 * @param groupsToExclude
	 * @param roleId - role that users should have in order to be returned
	 * @param unitIds
	 * @return
	 */
	PaginatedResult<ResourceVisibilityMember> searchUsersAndGroups(
			long orgId, String searchTerm, int limit, List<Long> usersToExclude, List<Long> groupsToExclude, long roleId,
			List<Long> unitIds);

	PaginatedResult<ResourceVisibilityMember> searchUsersInUnitsWithRole(long orgId, String searchTerm,
																		 int limit, List<Long> unitIds,
																		 List<Long> usersToExclude, long roleId);

	PaginatedResult<ResourceVisibilityMember> searchVisibilityUsers(long orgId, String searchTerm,
																	int limit, List<Long> unitIds,
																	List<Long> usersToExclude);

}
