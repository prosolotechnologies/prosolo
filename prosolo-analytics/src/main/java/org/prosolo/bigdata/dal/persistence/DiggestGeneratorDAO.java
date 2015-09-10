package org.prosolo.bigdata.dal.persistence;

 
//import java.util.Collection;
import java.util.Date;
import java.util.List;


import org.prosolo.common.domainmodel.course.Course;
//import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;

public interface DiggestGeneratorDAO extends GenericDAO{

	List<Long> getAllUsersIds();

	//FeedsPreferences getFeedsPreferences(User user);

	List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> subscribedRssSources, User user, Date dateFrom);

	 FeedsPreferences getFeedsPreferences(long userId);

	List<User> getFollowingUsers(Long userid);

	List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate);

	//Collection<Course> getAllActiveCourses();

	List<Long> getAllActiveCoursesIds();

	List<FeedEntry> getFeedEntriesForCourseParticipants(Course course, Date date);

	//FeedsPreferences getFeedsPreferences(long userId, Session session);

 

}
