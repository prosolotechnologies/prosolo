package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.HibernateUtil;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.util.date.DateUtil;

 


public class DiggestGeneratorDAOImpl extends GenericDAOImpl implements
	DiggestGeneratorDAO{
	
	
	
	private static Logger logger = Logger
			.getLogger(DiggestGeneratorDAO.class);
	
	public DiggestGeneratorDAOImpl(){
		setSession(HibernateUtil.getSessionFactory().openSession());
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public  List<Long> getAllUsersIds() {
		//Session session=openSession();
		String query = 
			"SELECT user.id " +
			"FROM User user " +
			"WHERE user.deleted = :deleted ";
		System.out.println("Query:"+query);
		//@SuppressWarnings("unchecked")
		List<Long> result =null;
		try{
			 result = session.createQuery(query)
					 .setParameter("deleted", false).list();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//finally{
			//session.close();
		//}
		

		if (result != null) {
			System.out.println("RESULTS:"+result.size());
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
		System.out.println("FEED PREFERENCES QUERY:"+query+" userid:"+userId);
	
		try{
			FeedsPreferences feedsPreferences = (FeedsPreferences) (FeedsPreferences) session.createQuery(query)
					.setParameter("userid", userId).uniqueResult();
			//System.out.println("RETURNING FEED PREFERENCES...");
			System.out.println("RETURNING FEED PREFERENCES..."+feedsPreferences.getId());
			return feedsPreferences;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		System.out.println("RETURNING NULL AS PREFERENCE");
		return null;
		
	}
	@Override
	public List<FeedEntry> getFeedEntriesFromSources(
			List<FeedSource> feedSources, User user, Date dateFrom) {
		//Session session=openSession();
		if (feedSources == null || feedSources.isEmpty()) {
			return new ArrayList<FeedEntry>();
		}
		System.out.println("Feed sources:"+feedSources.size());
		for(FeedSource fs:feedSources){
			System.out.println("FS:"+fs.getId()+" fs:"+fs.getLink());
		}
	 		String query = 
			"SELECT DISTINCT feedEntry " + 
			"FROM FeedEntry feedEntry " +
			"WHERE feedEntry.feedSource IN (:feedSources) " +
				"AND feedEntry.dateCreated > :dateFrom " + 
				"AND feedEntry.subscribedUser = :user " +  
			"ORDER BY feedEntry.relevance ASC, feedEntry.dateCreated DESC";
		
		//@SuppressWarnings("unchecked")
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
		System.out.println("FEED entries for users:"+users.size());
	 
		
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
		
		String query1 = 
			"SELECT DISTINCT entry " + 
			"FROM FeedEntry entry " +
			"WHERE entry.maker IN ( " +
									"SELECT DISTINCT enrollment.user " +
									"FROM CourseEnrollment enrollment " +
									"LEFT JOIN enrollment.course course " +
									"WHERE course = :course) " +
			"AND entry.dateCreated BETWEEN :dateFrom AND :dateTo " +
			"ORDER BY entry.dateCreated DESC";
		
		Date dateFrom = DateUtil.getDayBeginningDateTime(date);
		Date dateTo = DateUtil.getNextDay(date);
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedMessages1 = session.createQuery(query1)
				.setEntity("course", course)
				.setDate("dateFrom", dateFrom)
				.setDate("dateTo", dateTo)
				.list();
		
		
		String query2 = 
			"SELECT excluded " +
			"FROM Course course1 " +
			"LEFT JOIN course1.excludedFeedSources excluded " +
			"WHERE course1 = :course " +
				"AND excluded IS NOT NULL";
		
		@SuppressWarnings("unchecked")
		List<FeedSource> feedSources = session.createQuery(query2)
				.setEntity("course", course)
				.list();
		
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
				"AND day(post.dateCreated) = day(:date) " +
			"ORDER BY post.dateCreated DESC ";
		
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> result = session.createQuery(query)
				.setParameterList("hashtags", hashtags)
				.setDate("date", date)
				.list();

		return result;
	}
}
