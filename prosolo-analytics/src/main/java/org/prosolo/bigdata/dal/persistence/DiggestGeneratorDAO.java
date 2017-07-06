package org.prosolo.bigdata.dal.persistence;


import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

//import java.util.Collection;
//import org.prosolo.common.domainmodel.course.Course;

public interface DiggestGeneratorDAO extends GenericDAO{

	List<Long> getAllUsersIds();

	//FeedsPreferences getFeedsPreferences(User user);

	List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> subscribedRssSources, User user, Date dateFrom);

	 FeedsPreferences getFeedsPreferences(long userId);

	List<User> getFollowingUsers(Long userid);

	List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate);

	//Collection<Course> getAllActiveCourses();

	List<Tag> getSubscribedHashtags(User user);

	UserSettings getUserSettings(long userId);

	List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo,
			TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom,
			Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom,
			Date dateTo, TimeFrame timeFrame, int limit, int page);
	
	List<TwitterPostSocialActivity1> getTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, int limit, int page,
			Session session);
	
	long countTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, Session session);
	
	void incrementNumberOfUsersThatGotEmailForCredentialFeedDigest(
			List<Long> credIds, LocalDateTime from, LocalDateTime to, Session session);

	//FeedsPreferences getFeedsPreferences(long userId, Session session);

}
