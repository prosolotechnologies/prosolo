package org.prosolo.services.feeds;

import java.util.Date;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.services.feeds.data.CredentialFeedsData;
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

	FeedsPreferences addPersonalBlogRssSource(FeedsPreferences feedsPreferences, String link);

	FeedsPreferences addSubscribedRssSource(FeedsPreferences feedsPreferences, String link);
	
	FeedsPreferences addSubscribedRssSources(FeedsPreferences feedsPreferences, List<FeedSourceData> feedSources);

	FeedsPreferences removeSubscribedRssSource(FeedsPreferences feedsPreferences, String link);

	FeedsPreferences removePersonalBlogSource(FeedsPreferences feedsPreferences);

	Date getLatestFeedEntry(User user);
	
	Date getLatestFriendsRSSFeedDigestDate(User user);

	List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate);

	List<FeedEntry> getFeedEntriesForCredentialStudents(Credential1 credential, Date fromDate);

	List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity1> getMyTweetsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	List<TwitterPostSocialActivity1> getCourseTweetsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page);

	void sendEmailWithFeeds(User user);

	List<UserFeedSourceAggregate> getFeedSourcesForCredential(long courseId);

	List<FeedEntry> getFeedEntriesFromSources(List<FeedSource> subscribedRssSources, User user, Date dateFrom);

	List<CredentialFeedsData> getUserFeedsForCredential(long credId) throws DbConnectionException;
	
	List<CredentialFeedsData> getCredentialFeeds(long credId) throws DbConnectionException;
	
	void updateFeedLink(CredentialFeedsData feed) throws DbConnectionException;
}
