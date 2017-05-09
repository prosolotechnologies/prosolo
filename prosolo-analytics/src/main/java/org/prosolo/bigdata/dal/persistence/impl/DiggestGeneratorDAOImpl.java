package org.prosolo.bigdata.dal.persistence.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.digest.FilterOption;
import org.prosolo.common.web.digest.FeedsUtil;

 


public class DiggestGeneratorDAOImpl extends GenericDAOImpl implements
	DiggestGeneratorDAO{
	
	
	
	private static Logger logger = Logger
			.getLogger(DiggestGeneratorDAO.class);
	
	public DiggestGeneratorDAOImpl(){
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public  List<Long> getAllUsersIds() {
		//Session session=openSession();
		String query = 
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";
		List<Long> result =null;
		try{
			 result = session.createQuery(query)
					 .setParameter("deleted", false).list();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		if (result != null) {
			return result;
		}
		return new ArrayList<Long>();
	}

	@Override
	public FeedsPreferences getFeedsPreferences(long userId) {
		String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		try{
			FeedsPreferences feedsPreferences = (FeedsPreferences) (FeedsPreferences) session.createQuery(query)
					.setParameter("userid", userId).uniqueResult();
			return feedsPreferences;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
		
	}
	@Override
	public List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> feedSources, User user, Date dateFrom) {
		if (feedSources == null || feedSources.isEmpty()) {
			return new ArrayList<FeedEntry>();
		}
 		String query = 
			"SELECT DISTINCT feedEntry " + 
			"FROM FeedEntry feedEntry " +
			"WHERE feedEntry.feedSource IN (:feedSources) " +
				"AND feedEntry.dateCreated > :dateFrom " + 
				"AND feedEntry.subscribedUser = :user " +  
			"ORDER BY feedEntry.relevance ASC, feedEntry.dateCreated DESC";

		try{
			@SuppressWarnings("unchecked")
			List<FeedEntry> feedEntries = session.createQuery(query)
					.setParameterList("feedSources", feedSources)
					.setDate("dateFrom", dateFrom)
					.setEntity("user", user)
					.list();
			return feedEntries;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	return new ArrayList<FeedEntry>();
		
		
	}
	@Override
	public List<User> getFollowingUsers(Long userid) {
		String query = 
			"SELECT DISTINCT fUser " + 
			"FROM FollowedEntity fEnt " + 
			"LEFT JOIN fEnt.user user "+
			"JOIN fEnt.followedUser fUser " + 
			"WHERE user.id = :userid " +
			"ORDER BY fUser.name, fUser.lastname";
		
		@SuppressWarnings("unchecked")
		List<User> users = session.createQuery(query)
			.setLong("userid", userid)
			.list();
		if (users != null) {
			return users;
		}
		return new ArrayList<User>();
	}
	@Override
	public List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate) {
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedEntry entry " +
			"WHERE entry.maker IN (:users) ";
		
		if (fromDate != null) {
			query += "AND entry.dateCreated > :fromDate ";
		}
		
		query += "ORDER BY entry.dateCreated DESC";
		
		Query q = session.createQuery(query)
				.setParameterList("users", users);
		
		if (fromDate != null) {
			q.setDate("fromDate", fromDate);
		}
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedMessages = q.list();
		return feedMessages;
	}
	@Override
	public List<Long> getAllActiveCoursesIds() {
		String query = 
			"SELECT DISTINCT course.id " +
			"FROM Course course " +
			"WHERE course.published = :published ";
		
		@SuppressWarnings("unchecked")
		List<Long> result = session.createQuery(query).
				setBoolean("published", true).
				list();
		
		return result;
	}
	@Override
	public List<FeedEntry> getFeedEntriesForCourseParticipants(Course course, Date date) {
		//course = merge(course);
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedEntry entry " +
			"WHERE entry.maker IN ( " +
									"SELECT DISTINCT enrollment.user " +
									"FROM CourseEnrollment enrollment " +
									"LEFT JOIN enrollment.course course " +
									"WHERE course = :course) " +
				"AND entry.feedSource NOT IN ( " +
									"SELECT excluded " +
									"FROM Course course1 " +
									"LEFT JOIN course1.excludedFeedSources excluded " +
									"WHERE course1 = :course " +
										"AND excluded IS NOT NULL)";
		
		if (date != null) {
			query += "AND entry.dateCreated BETWEEN :dateFrom AND :dateTo ";
		}
		
		query += "ORDER BY entry.dateCreated DESC";
		
		Query q = session.createQuery(query)
				.setEntity("course", course);
		
		if (date != null) {
			Date dateFrom = DateUtil.getDayBeginningDateTime(date);
			Date dateTo = DateUtil.getNextDay(date);
			
			q.setDate("dateFrom", dateFrom);
			q.setDate("dateTo", dateTo);
		}
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedMessages = q.list();
		return feedMessages;
	}
	@Override
	public List<Tag> getSubscribedHashtags(User user) {
		String query = 
			"SELECT DISTINCT hashtag " + 
			"FROM TopicPreference topicPreference " +
			"LEFT JOIN topicPreference.user user " +
			"LEFT JOIN topicPreference.preferredHashtags hashtag "+
			"WHERE hashtag.id > 0";
		
		if (user != null) {
			query += " AND user = :user";
		}
		
		Query q = session.createQuery(query);
		
		if (user != null) {
			q.setEntity("user", user);
		}
		
		@SuppressWarnings("unchecked")
		List<Tag> result = q.list();
		
		if (result != null) {
			return result;
		} else {
			return new ArrayList<Tag>();
		}
	}
	@Override
	public List<TwitterPostSocialActivity> getTwitterPosts(Collection<Tag> hashtags, Date date) {
		if (hashtags == null || hashtags.isEmpty()) {
			return new ArrayList<TwitterPostSocialActivity>();
		}
		
		String query = 
			"SELECT DISTINCT post " +
			"FROM TwitterPostSocialActivity post " +
			"LEFT JOIN post.hashtags hashtag " + 
			"WHERE hashtag IN (:hashtags) " +
				 "AND year(post.dateCreated) = year(:date) " + 
				 "AND month(post.dateCreated) = month(:date) " + 
			"ORDER BY post.dateCreated DESC ";
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> result = session.createQuery(query)
				.setParameterList("hashtags", hashtags)
				 .setDate("date", date)
				.list();
		return result;
	}
	@Override
	public UserSettings getUserSettings(long userId) {
		String query = 
			"SELECT settings " +
			"FROM UserSettings settings " +
			"LEFT JOIN settings.user user " +
			"WHERE user.id = :userId";
		
		UserSettings result = (UserSettings) session.createQuery(query).
			setLong("userId", userId).
			uniqueResult();
		return result;
	}
	@Override
	public List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.debug("Loading my feeds for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.entries entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " + 
//				"AND feedsDigest.timeFrame = :timeFrame " +
				"AND feedsDigest.feedsSubscriber = :userId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.relevance ASC, entry.dateCreated DESC";
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = session.createQuery(query)
				.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.myfeeds))
//				.setString("timeFrame", timeFrame.name())
				.setDate("dateFrom", dateFrom)
				.setDate("dateTo", dateTo)
				.setLong("userId", userId)
				.setMaxResults(limit + 1)
				.setFirstResult((page - 1) * limit)
				.list();
		return feedEntries;
	}
	@Override
	public List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.debug("Loading friends feeds for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.entries entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " +  
				"AND feedsDigest.feedsSubscriber = :userId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.relevance ASC, entry.dateCreated DESC";
			
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = session.createQuery(query)
				.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.friendsfeeds))
				.setDate("dateFrom", dateFrom)
				.setDate("dateTo", dateTo)
				.setLong("userId", userId)
				.setMaxResults(limit + 1)
				.setFirstResult((page - 1) * limit)
				.list();
			return feedEntries;
	}
	@Override
	public List<TwitterPostSocialActivity> getMyTweetsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.debug("Loading my tweets for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.tweets entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " + 
				"AND feedsDigest.feedsSubscriber = :userId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> feedEntries = session.createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.mytweets))
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("userId", userId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		return feedEntries;
	}
	@Override
	public List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.debug("Loading course feeds for course: " + courseId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.entries entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " +  
				"AND feedsDigest.course.id = :courseId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.relevance ASC, entry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = session.createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.coursefeeds))
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("courseId", courseId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		return feedEntries;
	}
	@Override
	public List<TwitterPostSocialActivity> getCourseTweetsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.debug("Loading course tweets for course: " + courseId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.tweets entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated BETWEEN :dateFrom AND :dateTo " + 
//				"AND feedsDigest.timeFrame = :timeFrame " +
				"AND feedsDigest.course.id = :courseId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> feedEntries = session.createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.coursetweets))
//			.setString("timeFrame", timeFrame.name())
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("courseId", courseId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		return feedEntries;
	}
	
	@Override
	public List<TwitterPostSocialActivity1> getTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, int limit, int page, 
			Session session) {
		if (hashtags == null || hashtags.isEmpty()) {
			return new ArrayList<>();
		}
		
		String query = 
			"SELECT DISTINCT post " +
			"FROM TwitterPostSocialActivity1 post " +
			"LEFT JOIN post.hashtags hashtag " + 
			"WHERE hashtag.title IN (:hashtags) " +
			"AND post.dateCreated BETWEEN :from AND :to " + 
			"ORDER BY post.dateCreated DESC ";
		Query q = session.createQuery(query)
				.setParameterList("hashtags", hashtags)
				.setTimestamp("from", DateUtil.toDate(from))
				.setTimestamp("to", DateUtil.toDate(to)); 
		if(page > 0) {
			q.setFirstResult((page - 1) * limit);
		}
		if(limit > 0) {
			q.setMaxResults(limit + 1);
		}
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity1> result = q.list();
		return result;
	}
	
	@Override
	public long countTwitterPostSocialActivitiesContainingHashtags(
			List<String> hashtags, LocalDateTime from, LocalDateTime to, Session session) {
		if (hashtags == null || hashtags.isEmpty()) {
			return 0;
		}
		
		String query = 
			"SELECT COUNT(DISTINCT post.id) " +
			"FROM TwitterPostSocialActivity1 post " +
			"LEFT JOIN post.hashtags hashtag " + 
			"WHERE hashtag.title IN (:hashtags) " +
			"AND post.dateCreated BETWEEN :from AND :to";
		
		Date fromDate = DateUtil.toDate(from);
		Date toDate = DateUtil.toDate(to);
		Long no = (Long) session.createQuery(query)
				.setParameterList("hashtags", hashtags)
				.setTimestamp("from", fromDate)
				.setTimestamp("to", toDate)
				.uniqueResult();
		
		return no != null ? no : 0;
	}
	
	@Override
	public void incrementNumberOfUsersThatGotEmailForCredentialFeedDigest(
			List<Long> credIds, LocalDateTime from, LocalDateTime to, Session session) {
		if (credIds == null || credIds.isEmpty() || from == null || to == null) {
			throw new IllegalStateException();
		}
		
		String query = 
			"UPDATE CredentialTwitterHashtagsFeedsDigest digest " +
			"SET digest.numberOfUsersThatGotEmail = digest.numberOfUsersThatGotEmail + 1 " +
			"WHERE digest.credential.id IN (:credIds) " +
			"AND digest.from = :from " +
			"AND digest.to = :to";
		int no = session.createQuery(query)
				.setParameterList("credIds", credIds)
				.setTimestamp("from", DateUtil.toDate(from))
				.setTimestamp("to", DateUtil.toDate(to))
				.executeUpdate();
		
		logger.info("Number of credential feed digest counter increments " + no);
	}
}
