package org.prosolo.search;

import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.impl.TextSearchFilteredResponse;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilterValue;
import org.prosolo.search.util.competences.CompetenceStudentsSortOption;
import org.prosolo.search.util.credential.*;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface UserTextSearch extends AbstractManager {

	PaginatedResult<UserData> searchUsers(long orgId, String searchString,
										  int page, int limit, boolean loadOneMore,
										  Collection<Long> excludeUserIds);
	
	/**
	 * Returns list of students currently learning credential specified by {@code credId}.
	 *
	 * @param orgId
	 * @param searchTerm
	 * @param filter
	 * @param instructorFilter
	 * @param page
	 * @param limit
	 * @param credId
	 * @param sortOption
	 * @return
	 */
	TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilter.SearchFilter> searchCredentialMembers (
			long orgId, String searchTerm, CredentialMembersSearchFilter.SearchFilter filter, CredentialStudentsInstructorFilter instructorFilter, int page, int limit, long credId, CredentialMembersSortOption sortOption);
	
	PaginatedResult<InstructorData> searchInstructors (
			long orgId, String searchTerm, int page, int limit, long credId,
			InstructorSortOption sortOption, List<Long> excludedIds);

	PaginatedResult<UserData> searchUsersWithInstructorRole (long orgId, String searchTerm,
															 long credId, long roleId, List<Long> unitIds,
															 List<Long> excludedUserIds);
	
	PaginatedResult<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			long orgId, String searchTerm, long credId, long instructorId, StudentAssignSearchFilter.SearchFilter filter,
			int page, int limit);
	
	/**
	 * Returns users defined on a system level if {@code organizationId} is less than or equals 0 and users
	 * from organization with {@code organizationId} id otherwise
	 *
	 * Call {@link PaginatedResult#getAdditionalInfo()} to get search filters:
	 * under key 'filters' all filters can be retrieved with type {@code List<RoleFilter>},
	 * under key 'selectedFilter' applied filter can be retrieved with type {@code RoleFilter}.
	 * @param term
	 * @param page
	 * @param limit
	 * @param paginate
	 * @param roleId pass 0 if All filter and role id otherwise
	 * @param includeSystemUsers whether to include system users
	 * @param excludeIds usersToExclude
     * @param adminRoles
	 * @param organizationId
	 * @return
	 */
	PaginatedResult<UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate, long roleId, List<Role> adminRoles,
			boolean includeSystemUsers, List<Long> excludeIds, long organizationId);
	
	PaginatedResult<StudentData> searchCredentialMembersWithLearningStatusFilter (
			long orgId, String searchTerm, LearningStatus filter, int page, int limit, long credId,
			long userId, CredentialMembersSortOption sortOption);
	
	PaginatedResult<StudentData> searchUnenrolledUsersWithUserRole (
			long orgId, String searchTerm, int page, int limit, long credId, long userRoleId, List<Long> unitIds);

	/**
	 * Retrieves all users followed by the user with specified id and fulfilling the search term.
	 *
	 * @param orgId
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param userId
	 * @return
	 */
	PaginatedResult<UserData> searchUsersWithFollowInfo(long orgId, String searchTerm,
                                                        int page, int limit, long userId, UserSearchConfig searchConfig);

	PaginatedResult<UserData> searchUsersInGroups(
			long orgId, String searchTerm, int page, int limit, long groupId, boolean includeSystemUsers);

	/**
	 * Searches through credential members by their name and last name, except for the excluded ones.
	 *
	 * @param orgId
	 * @param searchTerm search term
	 * @param limit number of results to return
	 * @param credId credential id
	 * @param peersToExcludeFromSearch user ids to exclude from search
	 * @return response containing initialized UserData that matches the search.
	 */
	PaginatedResult<UserData> searchCredentialPeers(
			long orgId, String searchTerm, long limit, long credId, List<Long> peersToExcludeFromSearch);

	PaginatedResult<UserData> searchUsersLearningCompetence(
			long orgId, String searchTerm, int limit, long compId, List<Long> usersToExcludeFromSearch);
	
	/**
	 * Returns list of students currently learning competence specified by {@code compId}.
	 * 
	 * @param searchTerm
	 * @param compId
	 * @param filter
	 * @param sortOption
	 * @param page
	 * @param limit
	 * @return
	 */
	TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> searchCompetenceStudents (
			long orgId, String searchTerm, long compId, CompetenceStudentsSearchFilterValue filter,
			CompetenceStudentsSortOption sortOption, int page, int limit);

	PaginatedResult<UserData> searchUsers(long orgId, String searchTerm, int limit,List<UserData> usersToExcludeFromSearch ,List<Long> userRoles);

	/**
	 * Returns users belonging to organization with {@code orgId} id who have a role with {@code roleId} id
	 * and who are not already added to the unit with {@code unitId} id in that role.
	 *
	 * @param orgId
	 * @param unitId
	 * @param roleId
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param includeSystemUsers
	 * @return
	 */
	PaginatedResult<UserData> searchOrganizationUsersWithRoleNotAddedToUnit(
			long orgId, long unitId, long roleId, String searchTerm, int page, int limit,
			boolean includeSystemUsers);

	/**
	 * Returns users that are added to the unit with {@code unitId} id in a role with {@code roleId} id
	 *
	 * @param orgId
	 * @param unitId
	 * @param roleId
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param includeSystemUsers
	 * @return
	 */
	PaginatedResult<UserData> searchUnitUsersInRole(
			long orgId, long unitId, long roleId, String searchTerm, int page, int limit,
			boolean includeSystemUsers);

	PaginatedResult<UserData> searchUnitUsersNotAddedToGroup(long orgId, long unitId, long roleId,
															 long groupId, String searchTerm,
															 int page, int limit, boolean includeSystemUsers);

}
