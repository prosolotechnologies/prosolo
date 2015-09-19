package org.prosolo.bigdata.feeds.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.CourseRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.CourseTwitterHashtagsFeedsDigest;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.feeds.FriendsRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedRSSFeedsDigest;
import org.prosolo.common.domainmodel.feeds.SubscribedTwitterHashtagsFeedsDigest;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.FeedsPreferences;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;

//import org.prosolo.services.annotation.TagManager;
import org.prosolo.bigdata.feeds.FeedParser;
import org.prosolo.bigdata.feeds.FeedsAgregator;
import org.prosolo.bigdata.feeds.data.FeedData;
import org.prosolo.bigdata.feeds.data.FeedMessageData;
import org.prosolo.bigdata.feeds.ResourceTokenizer;
import org.prosolo.bigdata.similarity.WebPageRelevance;
import org.prosolo.bigdata.similarity.impl.WebPageRelevanceImpl;

import com.google.gson.Gson;
/*import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.htmlparser.WebPageContentExtractor;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.interaction.PostManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.twitter.TwitterSearchService;
import org.prosolo.similarity.ResourceTokenizer;
import org.prosolo.similarity.WebPageRelevance;
import org.prosolo.web.ApplicationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;*/
 

/**
 * @author Zoran Jeremic 2013-08-15
 * 
 */

public class FeedsAgregatorImpl implements FeedsAgregator {


	private static Logger logger = Logger.getLogger(FeedsAgregatorImpl.class);
	private DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
	private ResourceTokenizer resourceTokenizer=new ResourceTokenizerImpl();

	/*@Autowired private DefaultManager defaultManager;
	private WebPageRelevance webPageRelevance;
	@Autowired private ResourceTokenizer resourceTokenizer;
	@Autowired private WebPageContentExtractor webPageContentExtractor;
	@Autowired private FeedsManager feedsManager;
	@Autowired private CourseManager courseManager;
	@Autowired private TagManager tagManager;
	@Autowired private PostManager postManager;
	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private TwitterSearchService twitterSearchService;
	@Autowired private FeedParser feedParser;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private ApplicationBean applicationBean;
*/
	//private final int FLUSH_LIMIT = 5;
	//private FeedParser feedParser=new JavaxXmlStreamFeedParser();
	private FeedParser feedParser=new RomeFeedParser();
	private WebPageRelevance webPageRelevance=new WebPageRelevanceImpl();
	//private Gson gson=new Gson();
	
	@Override
	//@Transactional (readOnly = false)
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
		//Date latestFeedDigestDate = feedsManager.getLatestFriendsRSSFeedDigestDate(user);
		//date=new Date();
		if (followees != null && !followees.isEmpty()) {
			System.out.println("CHECKING USER:"+userid);
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
		System.out.println("Parsing RSS feed:"+link);
		List<FeedEntry> feedEntries=new ArrayList<FeedEntry>();
		FeedData feedData = feedParser.readFeed(link, feedSource.getLastCheck());
		Gson gson=new Gson();
		if (feedData != null && !feedData.getEntries().isEmpty()) {
			System.out.println("FEED ENTRIES:"+feedData.getEntries().size());
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
				

				if(blogOwner!=null)System.out.println("blog owner:"+blogOwner.getId());
				if(subscribedUser!=null)System.out.println("subscribed:"+subscribedUser.getId());
				System.out.println("SHOULD SAVE FEED ENTRY HERE:"+Thread.currentThread().getId()+" users: s: content: "+gson.toJson(feedMessageData));
				feedEntries.add(feedEntry);
			}
			logger.info("Collected blog entries from RSS feed source " + feedSource + ", total entries:" + totalCounter);
		}
		return feedEntries;
	}


	
	@Override
	//@Transactional (readOnly = false)
	@Deprecated
	public void generateDailySubscribedRSSFeedsDigest(User user, Date dateFrom) {
	 	/*user = feedsManager.merge(user);
		
		String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
		
		List<FeedSource> subscribedRssSources = feedsManager.getFeedsPreferences(user).getSubscribedRssSources();
		
			
		for (FeedSource feedSource : subscribedRssSources) {
			parseRSSFeed(null, user, feedSource, userTokenizedString);
		}
		
		List<FeedEntry> subscribedRSSFeedEntries = feedsManager.getFeedEntriesFromSources(subscribedRssSources, user, dateFrom);
		
		if (subscribedRSSFeedEntries != null && !subscribedRSSFeedEntries.isEmpty()) {
			SubscribedRSSFeedsDigest subscribedRSSFeedDigest = new SubscribedRSSFeedsDigest();
			subscribedRSSFeedDigest.setEntries(subscribedRSSFeedEntries);
			subscribedRSSFeedDigest.setDateCreated(new Date());
			subscribedRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
			subscribedRSSFeedDigest.setFeedsSubscriber(user);
			
			feedsManager.saveEntity(subscribedRSSFeedDigest);
			
			logger.info("Created feed digest of subscribed feeds for user " + user + "; total entries:" + subscribedRSSFeedEntries.size());
		} */
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
		System.out.println("TOKENIZED STRING:"+userTokenizedString);
		List<FeedSource> subscribedRssSources = diggestGeneratorDAO.getFeedsPreferences(userid).getSubscribedRssSources();
		System.out.println("HAVE SOURCES:"+subscribedRssSources.size());
		List<FeedEntry> subscribedRSSFeedEntries =new ArrayList<FeedEntry>();	
		for (FeedSource feedSource : subscribedRssSources) {
			System.out.println("PARSING FEED:"+feedSource.getLink());
		 	List<FeedEntry> entries=parseRSSFeed(null, user, feedSource, userTokenizedString);
		 	feedSource.setLastCheck(new Date());
		 	System.out.println("FINISHED PARSING FEED:"+feedSource.getLink());
		 	if(entries.size()>0){		 		 
		 		diggestGeneratorDAO.saveInBatch(entries);
		 		subscribedRSSFeedEntries.addAll(entries);
		 	}
		 
		 
		}
		//User user=null;
		//List<FeedEntry> subscribedRSSFeedEntries = diggestGeneratorDAO.getFeedEntriesFromSources(subscribedRssSources, user, dateFrom);
		System.out.println("KT-2:"+subscribedRSSFeedEntries.size());
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
		System.out.println("PARTICIPANS FEED ENTRIES NUMBER:"+participantsFeedEntries.size());
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
		//diggestGeneratorDAO.getEntityManager().getTransaction().commit();
		//session.flush();
		//session.close();*/
	}
	/*
	
	@Override
	//@Transactional (readOnly = false)
	public void generateDailySubscribedTwitterHashtagsDigest(User user, Date date) {
	/*	logger.debug("Aggregating subsscribed hashtags tweets for user " + user);

		user = feedsManager.merge(user);
		
		List<Tag> personalHashtags = tagManager.getSubscribedHashtags(user);
		
		if (personalHashtags != null && !personalHashtags.isEmpty()) {
			List<TwitterPostSocialActivity> tweetsWithHashtags = postManager.getTwitterPosts(personalHashtags, date);
				
			if (tweetsWithHashtags != null && !tweetsWithHashtags.isEmpty()) {
			
				SubscribedTwitterHashtagsFeedsDigest courseRSSFeedDigest = new SubscribedTwitterHashtagsFeedsDigest();
				courseRSSFeedDigest.setTweets(tweetsWithHashtags);
				courseRSSFeedDigest.setDateCreated(new Date());
				courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
				courseRSSFeedDigest.setFeedsSubscriber(user);
				
				feedsManager.saveEntity(courseRSSFeedDigest);
				
				logger.info("Created subscribed Twitter hashtag digest for user "  + user + "; total entries :" + tweetsWithHashtags.size());
			}
		}*/
	//}
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
				System.out.println("NUMBER OF TAGS:"+personalHashtags.size()+" FOUND TWEETS:"+tweetsWithHashtags.size());
				if (tweetsWithHashtags != null && !tweetsWithHashtags.isEmpty()) {
				System.out.println("TweetsWithHashtags...");
					SubscribedTwitterHashtagsFeedsDigest courseRSSFeedDigest = new SubscribedTwitterHashtagsFeedsDigest();
					courseRSSFeedDigest.setTweets(tweetsWithHashtags);
					courseRSSFeedDigest.setDateCreated(new Date());
					courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
					courseRSSFeedDigest.setFeedsSubscriber(user);
					System.out.println("TRYING TO SAVE DIGGEST");
					diggestGeneratorDAO.save(courseRSSFeedDigest);
					System.out.println("SAVED DIGGEST");
					logger.info("Created subscribed Twitter hashtag digest for user "  + user + "; total entries :" + tweetsWithHashtags.size());
				}
			}
		 }
	@Override
	//@Transactional (readOnly = false)
	public void generateDailyCourseTwitterHashtagsDigest(Long courseid, Date date) {
		Course course=null;
		try{
			course=(Course) diggestGeneratorDAO.load(Course.class, courseid);
		}catch(ResourceCouldNotBeLoadedException ex){
			ex.printStackTrace();
			return;
		}
	 	logger.debug("Aggregating course hashtags tweets for the course " + course);

		//course = defaultManager.merge(course);
		
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
	@Deprecated
	//@Transactional (readOnly = false)
	public void generateDailyCourseTwitterHashtagsDigest(Course course, Date date) {
	/*	logger.debug("Aggregating course hashtags tweets for the course " + course);

		course = defaultManager.merge(course);
		
		Collection<Tag> courseHashtags = course.getHashtags();
		
		if (courseHashtags != null && !courseHashtags.isEmpty()) {
			List<TwitterPostSocialActivity> tweetsWithHashtags = postManager.getTwitterPosts(courseHashtags, date);
				
			if (tweetsWithHashtags != null && !tweetsWithHashtags.isEmpty()) {
			
				CourseTwitterHashtagsFeedsDigest courseRSSFeedDigest = new CourseTwitterHashtagsFeedsDigest();
				courseRSSFeedDigest.setTweets(tweetsWithHashtags);
				courseRSSFeedDigest.setDateCreated(new Date());
				courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
				courseRSSFeedDigest.setCourse(course);
				
				feedsManager.saveEntity(courseRSSFeedDigest);
				
				logger.debug("Created subscribed Twitter hashtag digest for course "  + course + "; total entries :" + tweetsWithHashtags.size());
			}
		}*/
	}

 

	//@Override
//	public void generateDailyCourseRSSFeedsDigest(Course course, Date date) {
		// TODO Auto-generated method stub
		
	//}

/*	@Override
	public void generateDailyFriendsRSSFeedDigest(User user, Date date) {
		// TODO Auto-generated method stub
		
	}*/
	
}
