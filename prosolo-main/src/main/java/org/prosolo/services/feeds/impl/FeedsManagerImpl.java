package org.prosolo.services.feeds.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.email.generators.FeedsEmailGenerator;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.digest.FeedsUtil;
import org.prosolo.common.web.digest.FilterOption;
import org.prosolo.common.web.digest.data.FeedsDigestData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.email.EmailSender;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.feeds.data.CourseFeedsData;
import org.prosolo.services.feeds.data.UserFeedSourceAggregate;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.digest.DigestBean;
import org.prosolo.web.settings.data.FeedSourceData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-08-16
 * 
 */
@Service("org.prosolo.bigdata.feeds.FeedsManager")
public class FeedsManagerImpl extends AbstractManagerImpl implements FeedsManager {

	private static final long serialVersionUID = 4703893591461610872L;

	private static Logger logger = Logger.getLogger(FeedsManager.class);
	
	@Autowired private UserManager userManager;
	@Autowired private EmailSender emailSender;
	@Autowired private FeedSourceManager feedSourceManager;
	@Autowired private InterfaceSettingsManager interfaceSettingsManager;

	@Override
	@Transactional (readOnly = true)
	public FeedsPreferences getFeedsPreferences(User user) {
		String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"LEFT JOIN FETCH feedPreferences.subscribedRssSources rssSources "+
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		
		logger.debug("hb query:" + query);
		
		FeedsPreferences feedsPreferences = (FeedsPreferences) persistence.currentManager().createQuery(query)
				 .setLong("userid", user.getId())
				.uniqueResult();
		
		return feedsPreferences;
	}
	
	@Override
	@Transactional (readOnly = true)
	public FeedsPreferences getFeedsPreferences(long userId) {
		String query = 
			"SELECT DISTINCT feedPreferences " + 
			"FROM FeedsPreferences feedPreferences " + 
			"LEFT JOIN feedPreferences.user user " + 
			"LEFT JOIN FETCH feedPreferences.rssLinks rssLinks "+
			"WHERE user.id = :userid  " +
			"AND feedPreferences.class in ('FeedsPreferences')"	;
		

		FeedsPreferences feedsPreferences =null;
		try{
			 feedsPreferences = (FeedsPreferences) persistence.currentManager().createQuery(query)
					.setLong("userid", userId)
					.uniqueResult();	
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return feedsPreferences;
	}

	@Override
	@Transactional (readOnly = true)
	public Date getLatestFeedEntry(User user) {
		String query = 
			"SELECT entry.dateCreated " + 
			"FROM FeedEntry entry " + 
			"WHERE entry.maker = :user " + 
			"ORDER BY entry.dateCreated DESC";
		
		Date latestDate = (Date) persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.setMaxResults(1)
				.uniqueResult();

		return latestDate;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Date getLatestFriendsRSSFeedDigestDate(User user) {
		String query = 
			"SELECT friendsFeedDigest.dateCreated " + 
			"FROM FriendsRSSFeedsDigest friendsFeedDigest " + 
			"WHERE friendsFeedDigest.feedsSubscriber = :user " + 
			"ORDER BY friendsFeedDigest.dateCreated DESC";
		
		Date latestDate = (Date) persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setMaxResults(1)
			.uniqueResult();

		return latestDate;
	}
	
	@Override
	@Transactional (readOnly = true)
	public FeedsPreferences getSystemFeedsPreferences(){
		User systemUser = userManager.getUser(Settings.getInstance().config.init.defaultUser.email);
		return this.getFeedsPreferences(systemUser);
	}
	
	@Override
	@Transactional (readOnly = false)
	public FeedsPreferences addPersonalBlogRssSource(FeedsPreferences feedsPreferences, String link) {
		feedsPreferences = merge(feedsPreferences);
		
		FeedSource feedSource = feedSourceManager.getOrCreateFeedSource(null, link);
		
		feedsPreferences.setPersonalBlogSource(feedSource);
		
		return saveEntity(feedsPreferences);
	}
	
	@Override
	@Transactional (readOnly = false)
	public FeedsPreferences addSubscribedRssSource(FeedsPreferences feedsPreferences, String link) {
		feedsPreferences = merge(feedsPreferences);
		
		FeedSource feedSource = feedSourceManager.getOrCreateFeedSource("", link);
		
		feedsPreferences.getSubscribedRssSources().add(feedSource);
		
		return saveEntity(feedsPreferences);
	}
	
	public FeedsPreferences addSubscribedRssSources(FeedsPreferences feedsPreferences, List<FeedSourceData> feedSources) {
		feedsPreferences = merge(feedsPreferences);
		
		for (FeedSourceData feedSourceData : feedSources) {
			if (feedSourceData.isToAdd()) {
				FeedSource feedSource = feedSourceManager.getOrCreateFeedSource(feedSourceData.getTitle(), feedSourceData.getLink());
				
				feedsPreferences.getSubscribedRssSources().add(feedSource);
			}
		}
		
		return saveEntity(feedsPreferences);
	}
	
	@Override
	@Transactional (readOnly = false)
	public FeedsPreferences removeSubscribedRssSource(FeedsPreferences feedsPreferences, String link) {
		feedsPreferences = merge(feedsPreferences);
		
		Iterator<FeedSource> iterator = feedsPreferences.getSubscribedRssSources().iterator();
		
		while (iterator.hasNext()) {
			FeedSource feedSource = (FeedSource) iterator.next();
			
			if (feedSource.getLink().equals(link)) {
				iterator.remove();
				break;
			}
		}
		
		return saveEntity(feedsPreferences);
	}
	
	@Override
	@Transactional (readOnly = false)
	public FeedsPreferences removePersonalBlogSource(FeedsPreferences feedsPreferences) {
		feedsPreferences = merge(feedsPreferences);
		
		feedsPreferences.setPersonalBlogSource(null);
		
		return saveEntity(feedsPreferences);
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<FeedEntry> getFeedEntriesForUsers(List<User> users, Date fromDate) {
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedEntry entry " +
			"WHERE entry.maker IN (:users) ";
		
		if (fromDate != null) {
			query += "AND entry.dateCreated > :fromDate ";
		}
		
		query += "ORDER BY entry.dateCreated DESC";
		
		Query q = persistence.currentManager().createQuery(query)
				.setParameterList("users", users);
		
		if (fromDate != null) {
			q.setDate("fromDate", fromDate);
		}
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedMessages = q.list();
		
		return feedMessages;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<FeedEntry> getFeedEntriesForCourseParticipants(Course course, Date date) {
		course = merge(course);
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
		
		Query q = persistence.currentManager().createQuery(query)
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
		
		@SuppressWarnings({ "unchecked", "unused" })
		List<FeedEntry> feedMessages1 = persistence.currentManager().createQuery(query1)
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
		
		@SuppressWarnings({ "unchecked", "unused" })
		List<FeedSource> feedSources = persistence.currentManager().createQuery(query2)
				.setEntity("course", course)
				.list();
		
		return feedMessages;
	}
	
//	@Override
//	@Transactional
//	public List<FeedsDigest> filterAllDigests(long userId, long courseId, Date date, List<FilterOption> filters, TimeFrame timeFrame, int limit, int page) {
//		logger.info("Loading digests for user: " + userId + ", for date: " + date + ", with filters: " + filters + ", for timeFrame: " + timeFrame);
//		
//		List<String> digestClassNames = FeedsUtil.convertToDigestClassNames(filters);
//		
//		String query = 
//				"SELECT feedsDigest, entry " + 
//				"FROM FeedsDigest feedsDigest " +
//				"LEFT JOIN feedsDigest.entries entry " +
//				"WHERE " +
//					"feedsDigest.class IN (:digestClassNames) " +
//					"AND year(feedsDigest.dateCreated) = year(:date) " + 
//					"AND month(feedsDigest.dateCreated) = month(:date) " + 
//					"AND day(feedsDigest.dateCreated) = day(:date) " + 
//					"AND feedsDigest.timeFrame = :timeFrame " +
//					"AND entry IN () " +
//						"SELECT entry " +
//						"FROM FeedsDigest feedsDigest1 " +
//						"LEFT JOIN feedsDigest1.entries entry1 " +
//						"WHERE feedsDigest.id = feedsDigest1.id " +
//						"AND entry IN () ";
//		
//		if (userId > 0 ) {
//			query += "AND (feedsDigest.feedsSubscriber = :userId OR " +
//					"feedsDigest.feedsSubscriber IS NULL) ";
//		} else {
//			query += "AND feedsDigest.feedsSubscriber IS NULL ";
//		}
//		
//		query += "ORDER BY entry.dateCreated DESC";
//		
//		Query q = persistence.currentManager().createQuery(query)
//				.setParameterList("digestClassNames", digestClassNames)
//				.setString("timeFrame", timeFrame.name())
//				.setDate("date", date)
//				.setMaxResults(limit)
//				.setFirstResult((page - 1) * limit);
//		
//		if (userId > 0) {
//			q.setLong("userId", userId);
//		}
//		
//		@SuppressWarnings("unchecked")
//		List<FeedsDigest> feedDigests = q.list();
//		
//		return feedDigests;
//	}
	
	@Override
	@Transactional
	public List<FeedEntry> getMyFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.info("Loading my feeds for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
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
		System.out.println("getMyFeedsDigest query:"+query);
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = persistence.currentManager().createQuery(query)
				.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.myfeeds))
//				.setString("timeFrame", timeFrame.name())
				.setDate("dateFrom", dateFrom)
				.setDate("dateTo", dateTo)
				.setLong("userId", userId)
				.setMaxResults(limit + 1)
				.setFirstResult((page - 1) * limit)
				.list();
		System.out.println("FOUND my feeds:"+feedEntries.size()+" for user:"+userId+" from:"+dateFrom.toString()+" to:"+dateTo.toString());
		return feedEntries;
	}
	
	@Override
	@Transactional
	public List<FeedEntry> getMyFriendsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.info("Loading friends feeds for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
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
		System.out.println("MY FRIENDS FEEDS QUERY:"+query);
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = persistence.currentManager().createQuery(query)
				.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.friendsfeeds))
//				.setString("timeFrame", timeFrame.name())
				.setDate("dateFrom", dateFrom)
				.setDate("dateTo", dateTo)
				.setLong("userId", userId)
				.setMaxResults(limit + 1)
				.setFirstResult((page - 1) * limit)
				.list();
		System.out.println("FOUND my friends feeds:"+feedEntries.size()+" for user:"+userId+" from:"+dateFrom.toString()+" to:"+dateTo.toString()+" diggestClass:"+FeedsUtil.convertToDigestClassName(FilterOption.friendsfeeds)+" limit:"+limit+" page:"+page);
		return feedEntries;
	}
	
	@Override
	@Transactional
	public List<TwitterPostSocialActivity> getMyTweetsFeedsDigest(long userId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.info("Loading my tweets for user: " + userId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.tweets entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " + 
//				"AND feedsDigest.timeFrame = :timeFrame " +
				"AND feedsDigest.feedsSubscriber = :userId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<TwitterPostSocialActivity> feedEntries = persistence.currentManager().createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.mytweets))
//			.setString("timeFrame", timeFrame.name())
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("userId", userId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		System.out.println("FOUND my tweets feeds:"+feedEntries.size()+" for user:"+userId+" from:"+dateFrom.toString()+" to:"+dateTo.toString());
		return feedEntries;
	}
	
	@Override
	@Transactional
	public List<FeedEntry> getCourseFeedsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.info("Loading course feeds for course: " + courseId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
		String query = 
			"SELECT DISTINCT entry " + 
			"FROM FeedsDigest feedsDigest " +
			"LEFT JOIN feedsDigest.entries entry " +
			"WHERE " +
				"feedsDigest.class = :digestClassName " +
				"AND feedsDigest.dateCreated > :dateFrom " + 
				"AND feedsDigest.dateCreated < :dateTo " +  
//				"AND feedsDigest.timeFrame = :timeFrame " +
				"AND feedsDigest.course.id = :courseId " +
				"AND entry IS NOT NULL " +
			"ORDER BY entry.relevance ASC, entry.dateCreated DESC";
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = persistence.currentManager().createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.coursefeeds))
//			.setString("timeFrame", timeFrame.name())
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("courseId", courseId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		System.out.println("FOUND course feeds:"+feedEntries.size()+" for course:"+courseId+" from:"+dateFrom.toString()+" to:"+dateTo.toString());
		return feedEntries;
	}

	@Override
	@Transactional
	public List<TwitterPostSocialActivity> getCourseTweetsDigest(long courseId, Date dateFrom, Date dateTo, TimeFrame timeFrame, int limit, int page) {
		logger.info("Loading course tweets for course: " + courseId + ", from date: " + dateFrom  + ", to date: " + dateTo  + ", for timeFrame: " + timeFrame);
		
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
		List<TwitterPostSocialActivity> feedEntries = persistence.currentManager().createQuery(query)
			.setString("digestClassName", FeedsUtil.convertToDigestClassName(FilterOption.coursetweets))
//			.setString("timeFrame", timeFrame.name())
			.setDate("dateFrom", dateFrom)
			.setDate("dateTo", dateTo)
			.setLong("courseId", courseId)
			.setMaxResults(limit + 1)
			.setFirstResult((page - 1) * limit)
			.list();
		System.out.println("FOUND course tweets:"+feedEntries.size()+" for course:"+courseId+" from:"+dateFrom.toString()+" to:"+dateTo.toString());
		return feedEntries;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	@Transactional
	public void sendEmailWithFeeds(User user) {
		user = merge(user);
		
		FeedsPreferences feedsPreferences = getFeedsPreferences(user);
		
		TimeFrame interval = feedsPreferences.getUpdatePeriod();
		
		if (interval == null) {
			interval = TimeFrame.DAILY;
		}
		
		boolean toSendEmail = false;
		Date today = DateUtil.getDayBeginningDateTime(new Date());
		Date dateFrom = null;
		Date dateTo = null;
		
		switch (interval) {
			case DAILY:
				dateFrom = DateUtil.getDayBeginningDateTime(today);
				dateTo = DateUtil.getNextDay(today);
				
				toSendEmail = true;
				break;
				
			case WEEKLY:
				dateFrom = DateUtil.getWeekBeginningDate(today);
				
				if (dateFrom.equals(today)) {
					dateTo = DateUtil.getNextWeekBeginningDate(today);

					toSendEmail = true;
				}
				break;
				
			case MONTHLY:
				dateFrom = DateUtil.getMonthBeginningDate(today);
				
				if (dateFrom.equals(today)) {
					dateTo = DateUtil.getNextMonthBeginningDate(today);
					
					toSendEmail = true;
				}
				break;
		}
		
		if (toSendEmail) {
			Locale locale = interfaceSettingsManager.getOrCreateUserSettings(user).getLocaleSettings().createLocale(); 
			int limit = 10;
			long userId = user.getId();
			String dashedDate = DateUtil.getPrettyDate(new Date(), DateUtil.DASH_DATE);
			
			// TODO
			long courseId = 1;
			
			List<FeedsDigestData> feedsDigests = new ArrayList<FeedsDigestData>();
			
			for (FilterOption filterOption : FilterOption.values()) {
				try {
					FeedsDigestData feedsDigestData = new FeedsDigestData();
					
					// pretty name of the digest category
					String categoryName = ResourceBundleUtil.getMessage("digest.filterName.title."+filterOption, locale);
					feedsDigestData.setCategoryName(categoryName);
					feedsDigestData.setFilter(filterOption);
					
					switch (filterOption) {
						case myfeeds:
							List<FeedEntry> entries = getMyFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							DigestBean.addFeedEntries(feedsDigestData, entries, limit);
							break;
						case friendsfeeds:
							List<FeedEntry> entries1 = getMyFriendsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							DigestBean.addFeedEntries(feedsDigestData, entries1, limit);
							break;
						case mytweets:
							List<TwitterPostSocialActivity> entries2 = getMyTweetsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							DigestBean.addTweetEntries(feedsDigestData, entries2, limit);
							break;
						case coursefeeds:
							List<FeedEntry> entries3 = getCourseFeedsDigest(courseId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							DigestBean.addFeedEntries(feedsDigestData, entries3, limit);
							break;
						case coursetweets:
							List<TwitterPostSocialActivity> entries4 = getCourseTweetsDigest(courseId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							DigestBean.addTweetEntries(feedsDigestData, entries4, limit);
							break;
					}
					
					if (!feedsDigestData.getEntries().isEmpty())
						feedsDigests.add(feedsDigestData);
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
			}
			
			try {
				String email = user.getEmail();
				
				// If development mode, send only to developer email
				if (!feedsDigests.isEmpty() && 
						(!Settings.getInstance().config.application.developmentMode || 
						email.equals(Settings.getInstance().config.application.developmentEmail))) {
					emailSender.sendEmail(new FeedsEmailGenerator(user.getName(), feedsDigests, dashedDate, interval), email, "ProSolo Feed Digest");
				}
			} catch (MessagingException | IOException e) {
				logger.error(e);
			}
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<UserFeedSourceAggregate> getFeedSourcesForCourse(long courseId) {
		logger.debug("Loading feed sources for the course: " + courseId);
		
		String query = 
			"SELECT user, personalBlog " + 
			"FROM FeedsPreferences feedPreferences, CourseEnrollment enrollment " + 
			"LEFT JOIN feedPreferences.user user " + 
			"LEFT JOIN feedPreferences.personalBlogSource personalBlog "+
			"LEFT JOIN enrollment.course course " +
			"WHERE feedPreferences.class IN ('FeedsPreferences') "	+
				"AND user.id = enrollment.user " +
				"AND course.id = :courseId "	+
				"AND personalBlog IS NOT NULL "	+
			"ORDER BY personalBlog.title";
		
		@SuppressWarnings("unchecked")
		List<Object[]> result = persistence.currentManager().createQuery(query)
			.setLong("courseId", courseId)
			.list();
		
		if (result != null && !result.isEmpty()) {
			List<UserFeedSourceAggregate> userFeedSources = new ArrayList<UserFeedSourceAggregate>();
			
			try {
				Course course = loadResource(Course.class, courseId);
			
				for (Object[] res : result) {
					User user = (User) res[0];
					FeedSource feedSource = (FeedSource) res[1];
					
					// could not find the proper way to extract this data in the query
					boolean excluded = course.getExcludedFeedSources().contains(feedSource);
					
					userFeedSources.add(new UserFeedSourceAggregate(user, feedSource, !excluded));
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
			return userFeedSources;
		}
		
		return new ArrayList<UserFeedSourceAggregate>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<FeedEntry> getFeedEntriesFromSources(List<FeedSource> feedSources, User user, Date dateFrom) {
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
		
		@SuppressWarnings("unchecked")
		List<FeedEntry> feedEntries = persistence.currentManager().createQuery(query)
			.setParameterList("feedSources", feedSources)
			.setDate("dateFrom", dateFrom)
			.setEntity("user", user)
			.list();
		
		return feedEntries;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<CourseFeedsData> getUserFeedsForCourse(long courseId) throws DbConnectionException {
		try {
			logger.debug("Loading feed sources for the course: " + courseId);
	
			String query = 
				"SELECT user.name, user.lastname, personalBlog.link, personalBlog.id, " + 
			    "case when (personalBlog in (excludedFeeds)) then false else true end as included " +
				"FROM FeedsPreferences feedPreferences, CourseEnrollment enrollment " + 
				"LEFT JOIN feedPreferences.user user " + 
				"LEFT JOIN feedPreferences.personalBlogSource personalBlog " +
				"LEFT JOIN enrollment.course course " +
				"LEFT JOIN course.excludedFeedSources excludedFeeds " +
				"WHERE feedPreferences.class IN ('FeedsPreferences') "	+
					"AND user.id = enrollment.user " +
					"AND course.id = :courseId "	+
					"AND personalBlog IS NOT NULL "	+
				"ORDER BY personalBlog.title";
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = persistence.currentManager().createQuery(query)
				.setLong("courseId", courseId)
				.list();
			
			if (result != null && !result.isEmpty()) {
				List<CourseFeedsData> userFeedSources = new ArrayList<CourseFeedsData>();
				
				//Course course = loadResource(Course.class, courseId);
			
				for (Object[] res : result) {
					String firstName = (String) res[0];
					String lastName = (String) res[1];
					String feedLink = (String) res[2];
					long id = (long) res[3];
					boolean included = (boolean) res[4];
					
					// could not find the proper way to extract this data in the query
					//boolean excluded = course.getExcludedFeedSources().contains(feedSource);
					
					userFeedSources.add(new CourseFeedsData(firstName, lastName, id, feedLink, included));
				}
				
				return userFeedSources;
			}
			
			return new ArrayList<>();
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while loading course feeds");
		}
	}
	
	
	@Override
	@Transactional (readOnly = true)
	public List<CourseFeedsData> getCourseFeeds(long courseId) throws DbConnectionException {
		try {
			logger.debug("Loading feed sources for the course: " + courseId);
	
			String query = 
				"SELECT feed " + 
				"FROM Course course " + 
				"INNER JOIN course.blogs feed " + 
				"WHERE course.id = :courseId";
			
			@SuppressWarnings("unchecked")
			List<FeedSource> result = persistence.currentManager().createQuery(query)
				.setLong("courseId", courseId)
				.list();
			
				if(result == null) {
					return new ArrayList<>();
				}
				List<CourseFeedsData> feeds = new ArrayList<>();
				for(FeedSource fs : result) {
					CourseFeedsData data = new CourseFeedsData();
					data.setId(fs.getId());
					data.setFeedLink(fs.getLink());
					feeds.add(data);
				}
				return feeds;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while loading course feeds");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateFeedLink(CourseFeedsData feed) throws DbConnectionException {
		try {
			FeedSource feedSource = (FeedSource) persistence.currentManager().load(FeedSource.class, feed.getId());
			feedSource.setLink(feed.getFeedLink());
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving feed");
		}
	}

}
