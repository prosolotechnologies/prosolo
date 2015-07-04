package org.prosolo.services.feeds;

import java.util.Date;
import java.util.List;

import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.feeds.FeedEntry;
import org.prosolo.domainmodel.feeds.FeedSource;
import org.prosolo.domainmodel.user.TimeFrame;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.services.feeds.data.UserFeedSourceAggregate;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.settings.data.FeedSourceData;

/**
 * @author Zoran Jeremic 2013-08-16
 * 
 */
public interface FeedsManager extends AbstractManager {
	
	FeedsPreferences getFeedsPreferences(User user);
	
	FeedsPreferences getSystemFeedsPreferences();
	
	FeedsPreferences getFeedsPreferences(long userId);

	FeedsPreferences addPersonalBlogSource(FeedsPreferences feedsPreferences, FeedSourceData feedSourceData);

	FeedsPreferences addSubscribedRssSources(FeedsPreferences feedsPreferences, List<FeedSourceData> feedSources);

	FeedsPreferences removeSubscribedRssSource(FeedsPreferences feedsPreferences, String link);

	FeedsPreferences removePersonalBlogSource(FeedsPreferences feedsPreferences);

	Date getLatestFeedEntry(User user);
	
	Date getLatestFriendsRSSFeedDigestDate(User user);

	List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate);

	List<FeedEntry> getFeedEntriesForCourseParticipants(Course course, Date fromDate);

	List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity> getMyTweetsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity> getCourseTweetsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	void sendEmailWithFeeds(User user);

	List<UserFeedSourceAggregate> getFeedSourcesForCourse(long courseId);

	List<FeedEntry> getFeedEntriesFromSources(List<FeedSource> subscribedRssSources, User user, Date dateFrom);

}
