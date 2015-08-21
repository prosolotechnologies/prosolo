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
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.bigdata.feeds.DiggestManager;
//import org.prosolo.services.annotation.TagManager;
import org.prosolo.bigdata.feeds.FeedParser;
import org.prosolo.bigdata.feeds.FeedsAgregator;
import org.prosolo.bigdata.feeds.data.FeedData;
import org.prosolo.bigdata.feeds.data.FeedMessageData;
import org.prosolo.bigdata.feeds.ResourceTokenizer;
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
//@Transactional
//@Service("org.prosolo.bigdata.feeds.FeedsAggregator")
public class FeedsAgregatorImpl implements FeedsAgregator {


	private static Logger logger = Logger.getLogger(FeedsAgregatorImpl.class);
	private DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
	private ResourceTokenizer resourceTokenizer=new ResourceTokenizerImpl();

	/*@Autowired private DefaultManager defaultManager;
	@Autowired private WebPageRelevance webPageRelevance;
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
	private final int FLUSH_LIMIT = 5;


	
	@Override
	//@Transactional (readOnly = false)
	public void aggregatePersonalBlogOfUser(User user) {
		/*user = feedsManager.merge(user);
		
		FeedsPreferences feedsPreferences = feedsManager.getFeedsPreferences(user);
		FeedSource personalBlogSource = feedsPreferences.getPersonalBlogSource();

		if (personalBlogSource != null) {
			
			String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
			
			parseRSSFeed(user, user, personalBlogSource, userTokenizedString);
		}*/
	}

	private void parseRSSFeed(User blogOwner, User subscribedUser, FeedSource feedSource, String userTokenizedString) {
		/*String link = feedSource.getLink();
		
		FeedData feedData = feedParser.readFeed(link, feedSource.getLastCheck());
		
		if (feedData != null && !feedData.getEntries().isEmpty()) {
			int saveCounter = 0;
			int totalCounter = 0;
			
			for (FeedMessageData feedMessageData : feedData.getEntries()) {
				saveCounter++;
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
				
				feedEntry = feedsManager.saveEntity(feedEntry);
				
				if (saveCounter > FLUSH_LIMIT) {
					feedsManager.getPersistence().flush();
					feedsManager.getPersistence().clear();
					saveCounter = 0;
				}
			}
			
			feedSource.setLastCheck(new Date());
			feedsManager.saveEntity(feedSource);
			logger.info("Collected blog entries from RSS feed source " + feedSource + ", total entries:" + totalCounter);
		}*/
	}

	@Override
	//@Transactional (readOnly = false)
	public void generateDailyFriendsRSSFeedDigest(User user, Date date) {
	/*	List<User> followees = followResourceManager.getFollowingUsers(user);
		
		//Date latestFeedDigestDate = feedsManager.getLatestFriendsRSSFeedDigestDate(user);
		
		if (followees != null && !followees.isEmpty()) {
			List<FeedEntry> friendsFeedEntries = feedsManager.getFeedEntriesForUsers(followees, date);
			
			if (friendsFeedEntries != null && !friendsFeedEntries.isEmpty()) {
				FriendsRSSFeedsDigest friendsFeedDigest = new FriendsRSSFeedsDigest();
				friendsFeedDigest.setEntries(friendsFeedEntries);
				friendsFeedDigest.setDateCreated(new Date());
				friendsFeedDigest.setTimeFrame(TimeFrame.DAILY);
				friendsFeedDigest.setFeedsSubscriber(user);
				
				feedsManager.saveEntity(friendsFeedDigest);
				
				logger.info("Created friends feed digest for user " + user + "; total entries :" + friendsFeedEntries.size());
			}
		}*/
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
	public void generateDailySubscribedRSSFeedsDigestForUser(Long userid, Date dateFrom) {
		System.out.println("GENERATE RSS FOR USER:"+userid);
		User user=diggestGeneratorDAO.getEntityManager().find(User.class, userid);
		System.out.println("FOUND USER:"+user.getLastname()+" "+user.getName());
	 	//user = feedsManager.merge(user);
		
		String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
		
		List<FeedSource> subscribedRssSources = diggestGeneratorDAO.getFeedsPreferences(user).getSubscribedRssSources();
		
			
		for (FeedSource feedSource : subscribedRssSources) {
			parseRSSFeed(null, user, feedSource, userTokenizedString);
		}
		
		List<FeedEntry> subscribedRSSFeedEntries = diggestGeneratorDAO.getFeedEntriesFromSources(subscribedRssSources, user, dateFrom);
		
		if (subscribedRSSFeedEntries != null && !subscribedRSSFeedEntries.isEmpty()) {
			SubscribedRSSFeedsDigest subscribedRSSFeedDigest = new SubscribedRSSFeedsDigest();
			subscribedRSSFeedDigest.setEntries(subscribedRSSFeedEntries);
			subscribedRSSFeedDigest.setDateCreated(new Date());
			subscribedRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
			subscribedRSSFeedDigest.setFeedsSubscriber(user);
			
			diggestGeneratorDAO.persist(subscribedRSSFeedDigest);
			//feedsManager.saveEntity(subscribedRSSFeedDigest);
			
			logger.info("Created feed digest of subscribed feeds for user " + user + "; total entries:" + subscribedRSSFeedEntries.size());
		} /*/
	}
	
	@Override
	//@Transactional (readOnly = false)
	public void generateDailyCourseRSSFeedsDigest(Course course, Date date) {
		/*List<FeedEntry> participantsFeedEntries = feedsManager.getFeedEntriesForCourseParticipants(course, date);
		
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
				}
				
				feedsManager.saveEntity(cloneFeedEntry);
				
				courseFeedEntries.add(cloneFeedEntry);
			}
			
			if (courseFeedEntries != null && !courseFeedEntries.isEmpty()) {
				
				CourseRSSFeedsDigest courseRSSFeedDigest = new CourseRSSFeedsDigest();
				courseRSSFeedDigest.setEntries(courseFeedEntries);
				courseRSSFeedDigest.setDateCreated(new Date());
				courseRSSFeedDigest.setTimeFrame(TimeFrame.DAILY);
				courseRSSFeedDigest.setCourse(course);
				
				feedsManager.saveEntity(courseRSSFeedDigest);
				
				logger.info("Created course digest for course"  + course + "; total entries :" + courseFeedEntries.size());
			}
		}*/
	}
	
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
	}
	
	@Override
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

	@Override
	public void generateDailyCourseRSSFeedsDigest(Course course, Date date) {
		// TODO Auto-generated method stub
		
	}
	
}
