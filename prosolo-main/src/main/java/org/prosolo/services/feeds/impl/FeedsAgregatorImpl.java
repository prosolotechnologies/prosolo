package org.prosolo.services.feeds.impl;

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
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.feeds.FeedParser;
import org.prosolo.services.feeds.FeedsAgregator;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.feeds.data.FeedData;
import org.prosolo.services.feeds.data.FeedMessageData;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-08-15
 * 
 */
@Transactional
@Service("org.prosolo.services.feeds.FeedsAggregator")
public class FeedsAgregatorImpl implements FeedsAgregator {


	private static Logger logger = Logger.getLogger(FeedsAgregatorImpl.class);

	@Autowired private DefaultManager defaultManager;
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

	private final int FLUSH_LIMIT = 5;

//	@SuppressWarnings("rawtypes")
//	@Transactional
//	private List<FeedEntry> collectFeedEntriesFromLink(FeedSource feedSource, Date fromDate, String userTokenizedString) {
//		List<FeedEntry> entries = new ArrayList<FeedEntry>();
//		
//		if (feedSource != null) {
//			SyndFeedInput input = new SyndFeedInput();
//			SyndFeed inFeed = null;
//
//			URLConnection con = null;
//			InputStream in = null;
//			
//			try {
//				
//				int attempt = 0;
//				String link = feedSource.getLink();
//				
//				while (attempt < 5) {
//					try {
//						URL url = new URL(link);
//						con = url.openConnection();
//						con.setConnectTimeout(10000);
//						con.setReadTimeout(10000);
//						in = con.getInputStream();
//						
//						XmlReader xmlReader = null;
//						xmlReader = new XmlReader(url);
//						inFeed = input.build(xmlReader);
//						break;
//					} catch (FeedException e) {
//						logger.info("FeedException for:" + feedSource + e.getLocalizedMessage());
//						attempt++;
//					}
//				}
//				
//				int counter = 0;
//				
//				if (inFeed != null && !inFeed.getEntries().isEmpty()) {
//					List syndEntries = inFeed.getEntries();
//					
//					for (Object object : syndEntries) {
//						counter++;
//						
//						SyndEntry syndEntry = (SyndEntry) object;
//						Date publishedDate = syndEntry.getPublishedDate();
//						
//						if (publishedDate != null && publishedDate.after(fromDate)) {
//							float relevance = (float) 0.0;
//							
//							if (userTokenizedString != null && !userTokenizedString.equals("")) {
//								relevance = webPageRelevance.calculateWebPageRelevanceForUser(link, userTokenizedString);
//							}
//							
//							FeedEntry feedEntry = new FeedEntry();
//							feedEntry.setDateCreated(publishedDate);
//							feedEntry.setTitle(syndEntry.getTitle());
//							feedEntry.setLink(syndEntry.getLink());
//							feedEntry.setRelevance(relevance);
//							feedEntry.setFeedSource(feedSource);
//							
//							String description = syndEntry.getDescription().getValue();
//							
//							if (description != null) {
//								Document document = Jsoup.parse(description);
//								List<String> images = jsoupParser.getImages(document);
//								
//								if (images.size() > 0) {
//									String imageLink = images.get(0);
//									feedEntry.setImage(imageLink);
//								}
//	
//								String htmlFreeDescription = document.body().text();
//								feedEntry.setDescription(htmlFreeDescription);
//							}
//							
//							feedEntry = feedsManager.saveEntity(feedEntry);
//							
//							if (counter > 30) {
//								feedsManager.getPersistence().flush();
//								feedsManager.getPersistence().clear();
//								counter = 0;
//							}
//							
//							entries.add(feedEntry);
//						}
//					}
//				}
//			} catch (IllegalArgumentException e) {
//				logger.error("IllegalArgumentException for:" + feedSource,
//						e);
//			} catch (IOException e) {
//				logger.error("IOException for:" + feedSource, e);
//			} finally {
//				try {
//					if (in != null)
//						in.close();
//				} catch (IOException e) {
//					logger.error(e);
//				}
//			}
//		}
//		return entries;
//	}
	
	@Override
	@Transactional (readOnly = false)
	public void aggregatePersonalBlogOfUser(User user) {
		user = feedsManager.merge(user);
		
		FeedsPreferences feedsPreferences = feedsManager.getFeedsPreferences(user);
		FeedSource personalBlogSource = feedsPreferences.getPersonalBlogSource();

		if (personalBlogSource != null) {
			
			String userTokenizedString = resourceTokenizer.getTokenizedStringForUser(user);
			
			parseRSSFeed(user, user, personalBlogSource, userTokenizedString);
		}
	}

	private void parseRSSFeed(User blogOwner, User subscribedUser, FeedSource feedSource, String userTokenizedString) {
		String link = feedSource.getLink();
		
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
		}
	}

	@Override
	@Transactional (readOnly = false)
	public void generateDailyFriendsRSSFeedDigest(User user, Date date) {
		List<User> followees = followResourceManager.getFollowingUsers(user);
		
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
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void generateDailySubscribedRSSFeedsDigest(User user, Date dateFrom) {
		user = feedsManager.merge(user);
		
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
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void generateDailyCourseRSSFeedsDigest(Course course, Date date) {
		List<FeedEntry> participantsFeedEntries = feedsManager.getFeedEntriesForCourseParticipants(course, date);
		
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
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void generateDailySubscribedTwitterHashtagsDigest(User user, Date date) {
		logger.debug("Aggregating subsscribed hashtags tweets for user " + user);

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
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void generateDailyCourseTwitterHashtagsDigest(Course course, Date date) {
		logger.debug("Aggregating course hashtags tweets for the course " + course);

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
		}
	}
	
}
