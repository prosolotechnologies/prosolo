package org.prosolo.search;

import java.util.Collection;
import java.util.List;

import org.prosolo.search.impl.TextSearchFilteredResponse;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.search.impl.TextSearchResponse1;
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
	
	TextSearchResponse1<InstructorData> searchInstructors (
			String searchTerm, int page, int limit, long credId, 
			InstructorSortOption sortOption, List<Long> excludedIds);
	
	TextSearchResponse1<UserData> searchUsersWithInstructorRole (String searchTerm, 
			long credId, long roleId, List<Long> excludedUserIds);
	
	List<Long> getInstructorCourseIds (long userId);
	
	TextSearchResponse1<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			String searchTerm, long credId, long instructorId, CredentialMembersSearchFilterValue filter,
			int page, int limit);
	
	/**
	 * Call {@link TextSearchResponse1#getAdditionalInfo()} to get search filters: 
	 * under key 'filters' all filters can be retrieved with type {@code List<RoleFilter>},
	 * under key 'selectedFilter' applied filter can be retrieved with type {@code RoleFilter}.
	 * @param term
	 * @param page
	 * @param limit
	 * @param paginate
	 * @param roleId pass 0 if All filter and role id otherwise
	 * @param includeSystemUsers whether to include system users
	 * @param excludeIds usersToExclude
	 * @return
	 */
	TextSearchResponse1<UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate, long roleId, boolean includeSystemUsers, List<Long> excludeIds);
	
	TextSearchResponse1<StudentData> searchCredentialMembersWithLearningStatusFilter (
			String searchTerm, LearningStatus filter, int page, int limit, long credId, 
			long userId, CredentialMembersSortOption sortOption);
	
	TextSearchResponse1<StudentData> searchUnenrolledUsersWithUserRole (
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
	TextSearchResponse1<UserData> searchPeopleUserFollows(String searchTerm,
			int page, int limit, long userId);
	
	TextSearchResponse1<UserSelectionData> searchUsersInGroups(
			String searchTerm, int page, int limit, long groupId, boolean includeSystemUsers);

	/**
	 * Searches through credential members by their name and last name, except for the excluded ones.
	 * 
	 * @param searchTerm search term
	 * @param limit number of results to return
	 * @param credId credential id
	 * @param peersToExcludeFromSearch user ids to exclude from search
	 * @return response containing initialized UserData that matches the search.
	 */
	TextSearchResponse1<UserData> searchPeersWithoutAssessmentRequest(
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

	TextSearchResponse1<UserData> searchNewOwner(String searchTerm, int limit,
												 Long usersToExcludeFromSearch);
}
