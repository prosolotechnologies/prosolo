package org.prosolo.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.CredentialSearchFilter;
import org.prosolo.search.util.credential.CredentialSortOption;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.search.util.credential.LearningStatus;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.UserGroupData;
import org.prosolo.services.nodes.data.UserSelectionData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.web.search.data.SortingOption;

/**
 * @author Zoran Jeremic
 * @date Jul 1, 2012
 */

public interface TextSearch extends AbstractManager {

	TextSearchResponse searchUsers(String searchString,
			int page, int limit, boolean loadOneMore,
			Collection<Long> excludeUserIds);
	
	TextSearchResponse1<UserData> searchUsers1 (
			String term, int page, int limit, boolean paginate, List<Long> excludeIds);

	TextSearchResponse searchLearningGoals(
			String searchString, int page, int limit, boolean loadOneMore,
			Collection<LearningGoal> existingGoals);

//	TextSearchResponse searchCompetences(
//			String searchString, int page, int limit, boolean loadOneMore,
//			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc);
	
	TextSearchResponse searchActivities(
			String searchString, int page, int limit, boolean loadOneMore,
			long[] activitiesToExclude);

	List<Reminder> searchReminders(String searchString,
			ReminderStatus status, int page, int limit, boolean loadOneMore);

	/**
	 * @param searchQuery
	 * @param creatorType 
	 * @param page
	 * @param limit
	 * @param loadOneMore
	 * @param coursesToExclude
	 * @param filterTags 
	 * @param sortDateAsc 
	 * @return
	 */
	TextSearchResponse searchCourses(
			String searchQuery, CreatorType creatorType, int page, int limit, boolean loadOneMore,
			Collection<Course> excludeCourses, boolean published, List<Tag> filterTags, List<Long> courseIds,
			SortingOption sortTitleAsc, SortingOption sortDateAsc);

	/**
	 * @param searchQuery
	 * @param page
	 * @param limit
	 * @param loadOneMore
	 * @param tagsToExclude
	 * @return
	 */
	TextSearchResponse searchTags(String searchQuery, int page, int limit,
			boolean loadOneMore, Collection<Tag> tagsToExclude);
	
	TextSearchResponse1<StudentData> searchCredentialMembers (
			String searchTerm, CredentialMembersSearchFilterValue filter, int page, int limit, long credId, 
			long instructorId, CredentialMembersSortOption sortOption);
	
	TextSearchResponse1<InstructorData> searchInstructors (
			String searchTerm, int page, int limit, long credId, 
			InstructorSortOption sortOption, List<Long> excludedIds);
	
	Map<String, Object> searchUnassignedCourseMembers (
			String searchTerm, long courseId);
	
	TextSearchResponse1<UserData> searchUsersWithInstructorRole (String searchTerm, 
			long credId, long roleId, List<Long> excludedUserIds);
	
	List<Long> getInstructorCourseIds (long userId);
	
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
	 * @return
	 */
	TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption);
	
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
	 * @return
	 */
	TextSearchResponse1<org.prosolo.web.administration.data.UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate, long roleId);
	
	TextSearchResponse1<StudentData> searchCredentialMembersWithLearningStatusFilter (
			String searchTerm, LearningStatus filter, int page, int limit, long credId, 
			long userId, CredentialMembersSortOption sortOption);
	
	TextSearchResponse1<StudentData> searchUnenrolledUsersWithUserRole (
			String searchTerm, int page, int limit, long credId, long userRoleId);

	TextSearchResponse1<UserGroupData> searchUserGroups (
			String searchString, int page, int limit);
	
	TextSearchResponse1<UserGroupData> searchUserGroupsForUser (
			String searchString, long userId, int page, int limit);
	
	TextSearchResponse1<UserSelectionData> searchUsersInGroups(
			String searchTerm, int page, int limit, long groupId);
	
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
}
