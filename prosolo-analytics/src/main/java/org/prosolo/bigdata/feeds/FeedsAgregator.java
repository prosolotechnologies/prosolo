package org.prosolo.bigdata.feeds;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Zoran Jeremic 2013-08-17
 *
 */
public interface FeedsAgregator {

	//void aggregatePersonalBlogOfUser(User user);

//	void generateDailySubscribedRSSFeedsDigest(User user, Date date);

//	void generateDailyFriendsRSSFeedDigest(User user, Date date);

	//void generateDailyCourseRSSFeedsDigest(Course course, Date date);

	//void generateDailySubscribedTwitterHashtagsDigest(User user, Date date);
	
	//void generateDailyCourseTwitterHashtagsDigest(Course course, Date date);

	void generateDailySubscribedRSSFeedsDigestForUser(Long userid, Date dateFrom);

	void generateDailyFriendsRSSFeedDigest(Long userid, Date date);

	void generateDailyCourseRSSFeedsDigest(Long courseid, Date date);

	void generateDailySubscribedTwitterHashtagsDigestForUser(Long userid, Date dateFrom);

	void aggregatePersonalBlogOfUser(Long userid);

	void generateDailyCourseTwitterHashtagsDigest(Long courseid, Date date);
	
	void generateCredentialTwitterHashtagsDigest(Long credId, LocalDateTime dateCreated, 
			LocalDateTime from, LocalDateTime to);

	void sendEmailWithFeeds(Long userid, Date date);
	
	void sendEmailWithFeeds(long userId, LocalDateTime ldt, boolean sendWeekly);

}