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
import org.prosolo.search.util.credential.InstructorAssignedFilter;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.web.search.data.SortingOption;

/**
 * @author Zoran Jeremic
 * @date Jul 1, 2012
 */

public interface TextSearch extends AbstractManager {

	TextSearchResponse searchUsers(String searchString,
			int page, int limit, boolean loadOneMore,
			Collection<Long> excludeUserIds);

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
	
	Map<String, Object> searchCourseMembers (
			String searchTerm, InstructorAssignedFilter filter, int page, int limit, long courseId, 
			long instructorId, CredentialMembersSortOption sortOption);
	
	Map<String, Object> searchInstructors (
			String searchTerm, int page, int limit, long courseId, 
			SortingOption sortingOption, List<Long> excludedIds);
	
	Map<String, Object> searchUnassignedCourseMembers (
			String searchTerm, long courseId);
	
	Map<String, Object> searchUsersWithInstructorRole (String searchTerm, long courseId, long roleId);
	
	List<Long> getInstructorCourseIds (long userId);
	
	TextSearchResponse1<CompetenceData1> searchCompetences1(
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc);
	
	TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption);
	
	TextSearchResponse1<CredentialData> searchCredentialsForManager(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption);
}
