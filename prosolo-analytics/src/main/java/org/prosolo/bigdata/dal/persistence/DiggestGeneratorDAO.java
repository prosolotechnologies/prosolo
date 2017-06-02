package org.prosolo.bigdata.dal.persistence;

 
import java.time.LocalDateTime;
//import java.util.Collection;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
//import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.TimeFrame;
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

	List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo,
			TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom,
			Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity> getMyTweetsFeedsDigest(long userId,
			Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom,
			Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity> getCourseTweetsDigest(long courseId,
			Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);
	
	List<TwitterPostSocialActivity1> getTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, int limit, int page,
			Session session);
	
	long countTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, Session session);
	
	void incrementNumberOfUsersThatGotEmailForCredentialFeedDigest(
			List<Long> credIds, LocalDateTime from, LocalDateTime to, Session session);

	//FeedsPreferences getFeedsPreferences(long userId, Session session);

 

}
