package org.prosolo.bigdata.dal.persistence;

 
//import java.util.Collection;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
//import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
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

	List<Tag> getSubscribedHashtags(User user);

	List<TwitterPostSocialActivity> getTwitterPosts(Collection<Tag> hashtags,
			Date date);

	UserSettings getUserSettings(long userId);

	//FeedsPreferences getFeedsPreferences(long userId, Session session);

 

}
