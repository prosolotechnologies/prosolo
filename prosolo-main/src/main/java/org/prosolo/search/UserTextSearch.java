package org.prosolo.search;

import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.impl.TextSearchFilteredResponse;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilterValue;
import org.prosolo.search.util.competences.CompetenceStudentsSortOption;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.search.util.credential.LearningStatus;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.UserSelectionData;
import org.prosolo.services.nodes.data.instructor.InstructorData;

/**
 * 
 * @author stefanvuckovic
 *
 */
public interface UserTextSearch extends AbstractManager {

	TextSearchResponse searchUsers(String searchString,
			int page, int limit, boolean loadOneMore,
			Collection<Long> excludeUserIds);
	
	/**
	 * Returns list of students currently learning credential specified by {@code credId}.
	 * 
	 * @param searchTerm
	 * @param filter
	 * @param page
	 * @param limit
	 * @param credId
	 * @param instructorId
	 * @param sortOption
	 * @return
	 */
	TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilterValue> searchCredentialMembers (
			String searchTerm, CredentialMembersSearchFilterValue filter, int page, int limit, long credId, 
			long instructorId, CredentialMembersSortOption sortOption);
	
	PaginatedResult<InstructorData> searchInstructors (
			String searchTerm, int page, int limit, long credId, 
			InstructorSortOption sortOption, List<Long> excludedIds);
	
	PaginatedResult<UserData> searchUsersWithInstructorRole (String searchTerm,
                                                             long credId, long roleId, List<Long> excludedUserIds);

	List<Long> getInstructorCourseIds (long userId);
	
	PaginatedResult<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			String searchTerm, long credId, long instructorId, CredentialMembersSearchFilterValue filter,
			int page, int limit);
	
	/**
	 * Returns users defined on a system level if {@code organizationId} is greater than 0 and users
	 * from organization with {@code organizationId} otherwise
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
			String searchTerm, LearningStatus filter, int page, int limit, long credId, 
			long userId, CredentialMembersSortOption sortOption);
	
	PaginatedResult<StudentData> searchUnenrolledUsersWithUserRole (
			String searchTerm, int page, int limit, long credId, long userRoleId);

	/**
	 * Retrieves all users followed by the user with specified id and fulfilling the search term.
	 * 
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param userId
	 * @return
	 */
	PaginatedResult<UserData> searchPeopleUserFollows(String searchTerm,
                                                      int page, int limit, long userId);

	PaginatedResult<UserData> searchUsersInGroups(
			long orgId, String searchTerm, int page, int limit, long groupId, boolean includeSystemUsers);

	/**
	 * Searches through credential members by their name and last name, except for the excluded ones.
	 * 
	 * @param searchTerm search term
	 * @param limit number of results to return
	 * @param credId credential id
	 * @param peersToExcludeFromSearch user ids to exclude from search
	 * @return response containing initialized UserData that matches the search.
	 */
	PaginatedResult<UserData> searchPeersWithoutAssessmentRequest(
			String searchTerm, long limit, long credId, List<Long> peersToExcludeFromSearch);
	
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
			String searchTerm, long compId, CompetenceStudentsSearchFilterValue filter, 
			CompetenceStudentsSortOption sortOption, int page, int limit);

	PaginatedResult<UserData> searchUsers(String searchTerm, int limit,List<UserData> usersToExcludeFromSearch ,List<Long> userRoles);

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
