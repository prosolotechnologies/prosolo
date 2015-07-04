package org.prosolo.search;

import java.util.Collection;
import java.util.List;

import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.course.CreatorType;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.reminders.Reminder;
import org.prosolo.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.search.impl.TextSearchResponse;
import org.prosolo.services.general.AbstractManager;
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
	TextSearchResponse searchCourses(String searchQuery,
			CreatorType creatorType, int page, int limit, boolean loadOneMore,
			Collection<Course> coursesToExclude, boolean published, List<Tag> filterTags, 
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
}
