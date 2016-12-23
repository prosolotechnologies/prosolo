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
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
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

	TextSearchResponse searchCompetences(
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc);
	
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
	
	TextSearchResponse1<CompetenceData1> searchCompetences1(long userId, Role role,
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc);
	
	TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption);
	
	TextSearchResponse1<CredentialData> searchCredentialsForManager(
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
}
