package org.prosolo.bigdata.feeds.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.bigdata.dal.persistence.impl.FeedsDigestDAOImpl;
import org.prosolo.bigdata.email.EmailSender;
//import org.prosolo.services.annotation.TagManager;
import org.prosolo.bigdata.feeds.FeedParser;
import org.prosolo.bigdata.feeds.FeedsAgregator;
import org.prosolo.bigdata.feeds.ResourceTokenizer;
import org.prosolo.bigdata.feeds.data.FeedData;
import org.prosolo.bigdata.feeds.data.FeedMessageData;
import org.prosolo.bigdata.services.email.impl.EmailLinkGeneratorImpl;
import org.prosolo.bigdata.similarity.WebPageRelevance;
import org.prosolo.bigdata.similarity.impl.WebPageRelevanceImpl;
import org.prosolo.bigdata.utils.ResourceBundleUtil;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activitywall.old.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.CourseRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.CourseTwitterHashtagsFeedsDigest;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.feeds.FriendsRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedTwitterHashtagsFeedsDigest;
import org.prosolo.common.domainmodel.interfacesettings.LocaleSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.email.generators.FeedsEmailGenerator;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.common.web.digest.FilterOption;
import org.prosolo.common.web.digest.data.FeedEntryData;
import org.prosolo.common.web.digest.data.FeedsDigestData;

import com.google.gson.Gson;
 
 

/**
 * @author Zoran Jeremic 2013-08-15
 * 
 */

public class FeedsAgregatorImpl implements FeedsAgregator {


	private static Logger logger = Logger.getLogger(FeedsAgregatorImpl.class);
	private DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
	private ResourceTokenizer resourceTokenizer=new ResourceTokenizerImpl();

	private FeedParser feedParser=new RomeFeedParser();
	private WebPageRelevance webPageRelevance=new WebPageRelevanceImpl();
	
	@Override
	public void aggregatePersonalBlogOfUser(Long userid) {
		System.out.println("***************AGGREGATE PERSONAL BLOG FOR USER:"+userid);
		User user=null;
		try {
			user = diggestGeneratorDAO.load(User.class, userid);
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FeedsPreferences feedsPreferences = diggestGeneratorDAO.getFeedsPreferences(userid);
		FeedSource personalBlogSource = feedsPreferences.getPersonalBlogSource();

		if (personalBlogSource != null) {
			System.out.println("PARSING PERSONAL BLOG:"+personalBlogSource.getLink());
			String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
			
			List<FeedEntry> entries=parseRSSFeed(user, user, personalBlogSource, userTokenizedString);
			if(entries.size()>0){		 		 
		 		diggestGeneratorDAO.saveInBatch(entries);
			}
		} 
	}
	@Override
	public void generateDailyFriendsRSSFeedDigest(Long userid, Date date) {
		System.out.println("********************GENERATE DAILY FRIENDS RSS FEED DIGGEST FOR USER:"+userid);
	 	List<User> followees = diggestGeneratorDAO.getFollowingUsers(userid);
		User user=null;
		try {
			user = diggestGeneratorDAO.load(User.class, userid);
		} catch (ResourceCouldNotBeLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (followees != null && !followees.isEmpty()) {
			List<FeedEntry> friendsFeedEntries = diggestGeneratorDAO.getFeedEntriesForUsers(followees, date);
			System.out.println("USER:"+user.getId()+" Has followees:"+followees.size()+" Friends feeds Entries:"+friendsFeedEntries.size());
			if (friendsFeedEntries != null && !friendsFeedEntries.isEmpty()) {
				FriendsRSSFeedsDigest friendsFeedDigest = new FriendsRSSFeedsDigest();
				friendsFeedDigest.setEntries(friendsFeedEntries);
				friendsFeedDigest.setDateCreated(new Date());
				friendsFeedDigest.setTimeFrame(TimeFrame.DAILY);
				friendsFeedDigest.setFeedsSubscriber(user);
				
				diggestGeneratorDAO.save(friendsFeedDigest);
				
				logger.info("Created friends feed digest for user " + user + "; total entries :" + friendsFeedEntries.size());
			}
		} 
	}
	private List<FeedEntry> parseRSSFeed(User blogOwner, User subscribedUser, FeedSource feedSource, String userTokenizedString) {
		String link = feedSource.getLink();
		List<FeedEntry> feedEntries=new ArrayList<FeedEntry>();
		FeedData feedData = feedParser.readFeed(link, feedSource.getLastCheck());
		Gson gson=new Gson();
		if (feedData != null && !feedData.getEntries().isEmpty()) {
			int totalCounter = 0;
			for (FeedMessageData feedMessageData : feedData.getEntries()) {
				totalCounter++;
				
				FeedEntry feedEntry = new FeedEntry();
				
				feedEntry.setDateCreated(feedMessageData.getPubDate());
				feedEntry.setTitle(feedMessageData.getTitle());
				feedEntry.setDescription(feedMessageData.getDescription());
				feedEntry.setLink(feedMessageData.getLink());
				feedEntry.setImage(feedMessageData.getThumbnail());
				 feedEntry.setFeedSource(feedSource);
				feedEntry.setMaker(blogOwner);
				 feedEntry.setSubscribedUser(subscribedUser);

				float relevance = (float) 0.0;
				
				if (userTokenizedString != null && !userTokenizedString.equals("")) {
					relevance = webPageRelevance.calculateWebPageRelevanceForUser(link, userTokenizedString);
					feedEntry.setRelevance(relevance);
				}
				feedEntries.add(feedEntry);
			}
			logger.info("Collected blog entries from RSS feed source " + feedSource + ", total entries:" + totalCounter);
		}
		return feedEntries;
	}


	


	@Override
	//@Transactional
	public void generateDailySubscribedRSSFeedsDigestForUser(Long userid, Date dateFrom) {
		System.out.println("*******************GENERATE RSS FOR USER:"+userid);
		User user=null;
		try{
			user=(User) diggestGeneratorDAO.load(User.class, userid);
		}catch(ResourceCouldNotBeLoadedException ex){
			ex.printStackTrace();
			return;
		}
		
		String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
		List<FeedSource> subscribedRssSources = diggestGeneratorDAO.getFeedsPreferences(userid).getSubscribedRssSources();
		List<FeedEntry> subscribedRSSFeedEntries =new ArrayList<FeedEntry>();	
		for (FeedSource feedSource : subscribedRssSources) {
		 	List<FeedEntry> entries=parseRSSFeed(null, user, feedSource, userTokenizedString);
		 	feedSource.setLastCheck(new Date());
		 	if(entries.size()>0){		 		 
		 		diggestGeneratorDAO.saveInBatch(entries);
		 		subscribedRSSFeedEntries.addAll(entries);
		 	}
		 
		 
		}
		if (subscribedRSSFeedEntries != null && !subscribedRSSFeedEntries.isEmpty()) {
			SubscribedRSSFeedsDigest subscribedRSSFeedDigest = new SubscribedRSSFeedsDigest();
			subscribedRSSFeedDigest.setEntries(subscribedRSSFeedEntries);
			subscribedRSSFeedDigest.setDateCreated(new Date());
			subscribedRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
			subscribedRSSFeedDigest.setFeedsSubscriber(user);
			//System.out.println("RSS digest:"+gson.toJson(subscribedRSSFeedDigest));
			diggestGeneratorDAO.save(subscribedRSSFeedDigest);
			//feedsManager.saveEntity(subscribedRSSFeedDigest);
			
			logger.info("Created feed digest of subscribed feeds for user " + user + "; total entries:" + subscribedRSSFeedEntries.size());
	 	} //
	}
	
	@Override
	public void generateDailyCourseRSSFeedsDigest(Long courseid, Date date) {
		System.out.println("************************GENERATE DAILY COURSE RSS FEEDS DIGEST FOR COURSE:"+courseid);
		Course course=null;
		try{
			course=(Course) diggestGeneratorDAO.load(Course.class, courseid);
		}catch(ResourceCouldNotBeLoadedException ex){
			ex.printStackTrace();
			return;
		}
		List<FeedEntry> participantsFeedEntries = diggestGeneratorDAO.getFeedEntriesForCourseParticipants(course, date);
		List<FeedEntry> courseFeedEntries = new ArrayList<FeedEntry>();

		if (participantsFeedEntries != null && !participantsFeedEntries.isEmpty()) {
			String courseTokenizedString = resourceTokenizer.getTokenizedStringForCourse(course);
			
			// we need to duplicate all feed entries and calculate relevance for the course
			
			for (FeedEntry feedEntry : participantsFeedEntries) {
				FeedEntry cloneFeedEntry = new FeedEntry();
				cloneFeedEntry.setDateCreated(feedEntry.getDateCreated());
				cloneFeedEntry.setTitle(feedEntry.getTitle());
				cloneFeedEntry.setDescription(feedEntry.getDescription());
				cloneFeedEntry.setLink(feedEntry.getLink());
				cloneFeedEntry.setImage(feedEntry.getImage());
				cloneFeedEntry.setFeedSource(feedEntry.getFeedSource());
				cloneFeedEntry.setMaker(feedEntry.getMaker());
				
				if (feedEntry.getHashtags() != null)
					cloneFeedEntry.getHashtags().addAll(feedEntry.getHashtags());
				
				if (courseTokenizedString != null && !courseTokenizedString.equals("")) {
					double relevance = webPageRelevance.calculateWebPageRelevanceForUser(feedEntry.getLink(), courseTokenizedString);
					cloneFeedEntry.setRelevance(relevance);
					System.out.println("FEED:"+feedEntry.getDateCreated()+" relevance:"+relevance);
				}
				
				diggestGeneratorDAO.save(cloneFeedEntry);
				
				courseFeedEntries.add(cloneFeedEntry);
			}
			
			if (courseFeedEntries != null && !courseFeedEntries.isEmpty()) {
				
				CourseRSSFeedsDigest courseRSSFeedDigest = new CourseRSSFeedsDigest();
				courseRSSFeedDigest.setEntries(courseFeedEntries);
				courseRSSFeedDigest.setDateCreated(new Date());
				courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
				courseRSSFeedDigest.setCourse(course);
				
				diggestGeneratorDAO.save(courseRSSFeedDigest);
				
				logger.info("Created course digest for course"  + course + "; total entries :" + courseFeedEntries.size());
			}
		}
	}

	@Override
	public void generateDailySubscribedTwitterHashtagsDigestForUser(Long userid, Date dateFrom) {
			logger.debug("Aggregating subsscribed hashtags tweets for user " + userid);
			System.out.println("************************GENERATE TWITTER HASHTAGS FOR USER:"+userid);
			User user=null;
			try{
				user=(User) diggestGeneratorDAO.load(User.class, userid);
			}catch(ResourceCouldNotBeLoadedException ex){
				ex.printStackTrace();
				return;
			}
			List<Tag> personalHashtags = diggestGeneratorDAO.getSubscribedHashtags(user);
 			if (personalHashtags != null && !personalHashtags.isEmpty()) {
				List<TwitterPostSocialActivity> tweetsWithHashtags = diggestGeneratorDAO.getTwitterPosts(personalHashtags, dateFrom);
				if (tweetsWithHashtags != null && !tweetsWithHashtags.isEmpty()) {
					SubscribedTwitterHashtagsFeedsDigest courseRSSFeedDigest = new SubscribedTwitterHashtagsFeedsDigest();
					courseRSSFeedDigest.setTweets(tweetsWithHashtags);
					courseRSSFeedDigest.setDateCreated(new Date());
					courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
					courseRSSFeedDigest.setFeedsSubscriber(user);
					diggestGeneratorDAO.save(courseRSSFeedDigest);
					logger.info("Created subscribed Twitter hashtag digest for user "  + user + "; total entries :" + tweetsWithHashtags.size());
				}
			}
		 }
	@Override
	public void generateDailyCourseTwitterHashtagsDigest(Long courseid, Date date) {
		Course course=null;
		try{
			course=(Course) diggestGeneratorDAO.load(Course.class, courseid);
		}catch(ResourceCouldNotBeLoadedException ex){
			ex.printStackTrace();
			return;
		}
	 	logger.debug("Aggregating course hashtags tweets for the course " + course);
		Collection<Tag> courseHashtags = course.getHashtags();
		
		if (courseHashtags != null && !courseHashtags.isEmpty()) {
			List<TwitterPostSocialActivity> tweetsWithHashtags =  diggestGeneratorDAO.getTwitterPosts(courseHashtags, date);
				
			if (tweetsWithHashtags != null && !tweetsWithHashtags.isEmpty()) {
			
				CourseTwitterHashtagsFeedsDigest courseRSSFeedDigest = new CourseTwitterHashtagsFeedsDigest();
				courseRSSFeedDigest.setTweets(tweetsWithHashtags);
				courseRSSFeedDigest.setDateCreated(new Date());
				courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
				courseRSSFeedDigest.setCourse(course);
				
				diggestGeneratorDAO.save(courseRSSFeedDigest);
				
				logger.info("Created subscribed Twitter hashtag digest for course "  + course + "; total entries :" + tweetsWithHashtags.size());
			}
		} 
	}
	@Override
	public void sendEmailWithFeeds(Long userid, Date date) {
		//user = merge(user);
		 
		User user=null;
		try{
			user=(User) diggestGeneratorDAO.load(User.class, userid);
		}catch(ResourceCouldNotBeLoadedException ex){
			ex.printStackTrace();
			return;
		}
		FeedsPreferences feedsPreferences = diggestGeneratorDAO.getFeedsPreferences(userid);
		
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
			UserSettings userSettings=diggestGeneratorDAO.getUserSettings(userid);
			EmailSender emailSender=new EmailSender();
			Locale locale = null;
			if(userSettings!=null){
				locale=userSettings.getLocaleSettings().createLocale();
			}else{
				locale=new LocaleSettings("en", "US").createLocale();
			}
				
			//Locale locale = diggestGeneratorDAO.getUserSettings(user).getLocaleSettings().createLocale(); 
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
							List<FeedEntry> entries = diggestGeneratorDAO.getMyFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							addFeedEntries(feedsDigestData, entries, limit, "personal_feeds", userId);
							break;
						case friendsfeeds:
							List<FeedEntry> entries1 = diggestGeneratorDAO.getMyFriendsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							addFeedEntries(feedsDigestData, entries1, limit, "friends_feeds", userId);
							break;
						case mytweets:
							List<TwitterPostSocialActivity> entries2 = diggestGeneratorDAO.getMyTweetsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							addTweetEntries(feedsDigestData, entries2, limit, "personal_tweets", userId);
							break;
						case coursefeeds:
							List<FeedEntry> entries3 = diggestGeneratorDAO.getCourseFeedsDigest(courseId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							addFeedEntries(feedsDigestData, entries3, limit, "course_feeds", userId);
							break;
						case coursetweets:
							List<TwitterPostSocialActivity> entries4 = diggestGeneratorDAO.getCourseTweetsDigest(courseId, dateFrom, dateTo, interval, limit, feedsDigestData.getPage());
							
							addTweetEntries(feedsDigestData, entries4, limit, "course_tweets", userId);
							break;
					}
					
					if (!feedsDigestData.getEntries().isEmpty())
						feedsDigests.add(feedsDigestData);
				} catch (KeyNotFoundInBundleException e) {
					logger.error(e);
				}
			}
			
			String email = user.getEmail();
			System.out.println("FEEDS DIGEST TO SEND:"+feedsDigests.size()+" for user:"+user.getId()+" email:"+email);
			// If development mode, send only to developer email
			if(!feedsDigests.isEmpty()){
				if(CommonSettings.getInstance().config.appConfig.developmentMode){
					email=CommonSettings.getInstance().config.appConfig.developerEmail;
				}
				System.out.println("SENDING EMAIL TO:"+email+" FOR USER:"+user.getName());
				 try {
					FeedsEmailGenerator emailGenerator = new FeedsEmailGenerator(user.getName(), feedsDigests, dashedDate, interval);
					emailSender.sendEmail(emailGenerator, email);
					System.out.println("EMAIL  SENT TO:"+email+" FOR USER:"+user.getName());
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			if (!feedsDigests.isEmpty() && 
//					(!CommonSettings.getInstance().config.appConfig.developmentMode || 
//					email.equals(CommonSettings.getInstance().config.appConfig.developmentEmail))) {
//				System.out.println("SENDING EMAIL TO:"+email+" FOR USER:"+user.getName());
//				//emailSender.sendEmail(new FeedsEmailGenerator(user.getName(), feedsDigests, dashedDate, interval), email, "ProSolo Feed Digest");
//			}
		}
		 
	}
	private void addFeedEntries(FeedsDigestData feedsDigestData, List<FeedEntry> entries, int limit,
			String context, long userId) {
		// if there is more than limit, set moreToLoad to true
		if (entries.size() == limit + 1) {
			entries = entries.subList(0, entries.size() - 1);
			feedsDigestData.setMoreToLoad(true);
		} else {
			feedsDigestData.setMoreToLoad(false);
		}
		
		for (FeedEntry feedEntry : entries) {
			long feedsDigestId = FeedsDigestDAOImpl.getInstance().getFeedsDigestIdForFeedEntry(
					feedEntry.getId());
			LinkedHashMap<String, Long> ctxParams = new LinkedHashMap<>();
			ctxParams.put("news_digest", feedsDigestId);
			ctxParams.put(context, feedEntry.getId());
			String link = EmailLinkGeneratorImpl.getInstance().getLink(userId, ctxParams);
			feedsDigestData.addEntry(new FeedEntryData(feedEntry, link));
		}
	}
	public  void addTweetEntries(FeedsDigestData feedsDigestData, List<TwitterPostSocialActivity> entries, 
			int limit, String context, long userId) {
		// if there is more than limit, set moreToLoad to true
		if (entries.size() == limit + 1) {
			entries = entries.subList(0, entries.size() - 1);
			feedsDigestData.setMoreToLoad(true);
		} else {
			feedsDigestData.setMoreToLoad(false);
		}
		
		for (TwitterPostSocialActivity tweet : entries) {
			long feedsDigestId = FeedsDigestDAOImpl.getInstance().getFeedsDigestIdForTweet(
					tweet.getId());
			LinkedHashMap<String, Long> ctxParams = new LinkedHashMap<>();
			ctxParams.put("news_digest", feedsDigestId);
			ctxParams.put(context, tweet.getId());
			String link = EmailLinkGeneratorImpl.getInstance().getLink(userId, ctxParams);
			feedsDigestData.addEntry(createFeedEntryData(tweet, link));
		}
	}
	private FeedEntryData createFeedEntryData(TwitterPostSocialActivity tweetEntry, String link) {
		FeedEntryData feedEntryData=new FeedEntryData();
		
		feedEntryData.setId(tweetEntry.getId());
		feedEntryData.setTitle(tweetEntry.getText());
		//feedEntryData.setLink(tweetEntry.getPostUrl());
		feedEntryData.setLink(link);
		feedEntryData.setDate(DateUtil.getPrettyDate(tweetEntry.getDateCreated()));
		
		if (tweetEntry.getMaker() != null)
			feedEntryData.setMaker(createUserData(tweetEntry.getMaker()));
		return feedEntryData;
	}
	private UserData createUserData(User user) {
		if (user != null){
			UserData userData=new UserData();
			userData.setId(user.getId());
			userData.setName(user.getName() + ((user.getLastname() != null) ? " " + user.getLastname() : ""));
			userData.setProfileUrl(user.getProfileUrl());
			userData.setAvatarUrl(getAvatarUrlInFormat(user, ImageFormat.size120x120));
			
			if (user.getUserType().equals(UserType.TWITTER_USER)) {
				userData.setPosition("Twitter User");
				userData.setExternalUser(true);
			} else {
				userData.setPosition(user.getPosition());
			}
			userData.setLocationName(user.getLocationName());
			if(user.getLatitude()!=null)
				userData.setLatitude(String.valueOf(user.getLatitude()));
			if(user.getLongitude()!=null)
				userData.setLongitude(String.valueOf(user.getLongitude()));
			return userData;
		}
		return null;
		
	}
private String getAvatarUrlInFormat(User user, ImageFormat format) {
		
		String avatarUrl = null;

		if (user != null) {
			// check if avatar is already full URL
			if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("http")) {
				return user.getAvatarUrl();
			}

		 
			avatarUrl = user.getAvatarUrl();
		}
		//else {
			//avatarUrl = getDefaultAvatarUrl();
		//}
		//return getAvatarUrlInFormat(avatarUrl, format);
		return avatarUrl;
	}
 

 

	
}
