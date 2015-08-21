package org.prosolo.bigdata.feeds.impl;


 
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.feeds.DiggestManager;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.bigdata.feeds.FeedsAgregator;
//import org.prosolo.services.feeds.FeedsManager;
//import org.prosolo.services.nodes.CourseManager;
//import org.prosolo.services.nodes.UserManager;
import org.prosolo.common.util.date.DateUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;


/**
 * @author Zoran Jeremic 2013-08-29
 *
 */
//@Service("org.prosolo.bigdata.feeds.DiggestManager")
public class DiggestManagerImpl implements DiggestManager {
	
	private static Logger logger = Logger.getLogger(DiggestManagerImpl.class);
	
	//@Autowired private UserManager userManager;
	//@Autowired private CourseManager courseManager;
	//@Autowired private FeedsManager feedsManager;
	//@Autowired private FeedsAgregator feedsAgregator;
	
	@Override
	public void initializeDiggestJobs() {
		
	}
	
	@Override
	public void createFeedDiggestsAndSendEmails() {
/*		Calendar cal = Calendar.getInstance();
//		cal.set(2015, 5, 2);
		
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		yesterday = DateUtil.getDayBeginningDateTime(yesterday);
		
		
		 * Friends RSS Feed Digest
		 
		this.createDailyFriendsRSSFeedDigests(yesterday);
		
		
		 * Subscribed RSS Feeds
		 
		this.createDailyUserSubscribedRSSFeedDigests(yesterday);
		
		this.createDailyCourseFeedsDigests(yesterday);
		
		this.createDailySubscribedHashtagsDigests(yesterday);
		this.createDailyCourseHashtagsDigests(yesterday);
		
		// send email
		sendEmailsWithFeedDigests();
		
//		this.createSystemFeeds();
//		this.createPrivateFeeds();
//		this.collectTwitterHashTags();
//		this.createFriendsFeeds();
*/		
	}

	private void createDailyFriendsRSSFeedDigests(Date date) {
/*		logger.debug("Generating friends feed digests");
		
		Collection<User> users = userManager.getAllUsers();
		
		this.agregateUserPersonalBlogs(users);
		
		for (User user : users) {
			feedsAgregator.generateDailyFriendsRSSFeedDigest(user, date);
		}*/
	}

	private void agregateUserPersonalBlogs(Collection<User> users) {
	/*	for (User user : users) {
			if (!user.isSystem()) {
				try {
					feedsAgregator.aggregatePersonalBlogOfUser(user);
				} catch (Exception ex) {
					logger.error("Didn't process personal feed for user: " + user, ex);
				}
			}
		}*/
	}
	
	private void createDailyUserSubscribedRSSFeedDigests(Date date) {
	/*	logger.debug("Generating subscribed RSS feed digests");
		
		Collection<User> users = userManager.getAllUsers();
		
		for (User user : users) {
			if (!user.isSystem()) {
				try {
					feedsAgregator.generateDailySubscribedRSSFeedsDigest(user, date);
				} catch (Exception ex) {
					logger.error("Didn't process personal feed for user:" + user.getLastname() + " " + user.getName(), ex);
				}
			}
		}*/
	}

	private void createDailyCourseFeedsDigests(Date date) {
	/*	logger.debug("Generating course feed digests");
		
		Collection<Course> users = courseManager.getAllActiveCourses();
		
		for (Course course : users) {
			feedsAgregator.generateDailyCourseRSSFeedsDigest(course, date);
		}*/
	}

	private void createDailySubscribedHashtagsDigests(Date date) {
	/*	logger.debug("Generating subscribed hashtags digests");
		
		Collection<User> users = userManager.getAllUsers();
		
		for (User user : users) {
			if (!user.isSystem()) {
				feedsAgregator.generateDailySubscribedTwitterHashtagsDigest(user, date);
			}
		}*/
	}

	private void createDailyCourseHashtagsDigests(Date date) {
	/*	logger.debug("Generating course hashtags digests");
		
		Collection<Course> users = courseManager.getAllActiveCourses();
		
		for (Course course : users) {
			feedsAgregator.generateDailyCourseTwitterHashtagsDigest(course, date);
		}*/
	}

	private void sendEmailsWithFeedDigests() {
	/*	Collection<User> users = userManager.getAllUsers();
		
		for (User user : users) {
			if (!user.isSystem()) {
				try {
					feedsManager.sendEmailWithFeeds(user);
				} catch (Exception ex) {
					logger.error("Didn't process personal feed for user:" + user.getLastname() + " " + user.getName(), ex);
				}
			}
		}*/
	}

//	private void createSystemFeeds() {
//		TimeFrame timeFrame = TimeFrame.DAILY;
//		
//		String systemUserEmail = Settings.getInstance().config.init.defaultUser.email;
//		User systemUser = userManager.getUser(systemUserEmail);
//		PersonalFeed systemFeed = feedsAgregator.aggregateSystemFeeds(systemUser.getId(), timeFrame);
//		int failedPersonalFeeds = 0;
//		int successfullPersonalFeeds = 0;
//		
//		if (systemFeed.getFeedMessages().isEmpty() && systemFeed.getTagsTweets().isEmpty()) {
//			logger.debug("Nothing to send as daily diggest. System PersonalFeed is empty.");
//			return;
//		}
//		
//		Collection<User> users = userManager.getAllUsers();
//		logger.info("Create Daily Diggest for System user STARTED:" + users.size());
//		
//		for (User user : users) {
//			
//			if (!user.isSystem()) {
//				FeedsPreferences feedsPreferences = feedsManager.getFeedsPreferences(user);
//				
//				if (feedsPreferences.getUpdatePeriod() == null) {
//					feedsPreferences.setUpdatePeriod(TimeFrame.DAILY);
//					feedsManager.saveEntity(feedsPreferences);
//				}
//				
//				if (feedsPreferences.getUpdatePeriod() != null && feedsPreferences.getUpdatePeriod().equals(timeFrame)) {
//					boolean success = feedsAgregator.createNewPersonalFeed(user, timeFrame, systemFeed);
//				
//					if (success) {
//						successfullPersonalFeeds++;
//					} else {
//						failedPersonalFeeds++;
//					}
//					
//					logger.info("Creating System feed digest for user:" + user.getId() + " lastname:" + user.getLastname() + " firstname:"
//							+ user.getName() + " success:" + success);
//				}
//			}
//		}
//		
//		logger.info("Create Daily Diggest for System user FINISHED FOR:" + users.size() + " FAILED:" + failedPersonalFeeds + " SUCCESS:"
//				+ successfullPersonalFeeds);
//	}
	
//	private void createPrivateFeeds() {
//		Collection<User> users = userManager.getAllUsers();
//		int failedPersonalFeeds = 0;
//		int successfullPersonalFeeds = 0;
//		
//		for (User user : users) {
//			if (!user.isSystem()) {
//				boolean success = false;
//				
//				try {
//					success = feedsAgregator.aggregatePersonalBlogOfUser(user);
//				} catch (Exception ex) {
//					logger.error("Didn't process personal feed for user:" + user.getLastname() + " " + user.getName(), ex);
//				}
//				
//				if (success) {
//					successfullPersonalFeeds++;
//				} else {
//					failedPersonalFeeds++;
//				}
//				
//				logger.info("Update daily digest with Private feeds for user:" + user.getId() + " lastname:" + user.getLastname() + " firstname:"
//						+ user.getName() + " success:" + success);
//			}
//		}
//		logger.info("Create Daily Diggest Private Feeds FINISHED FOR:" + users.size() + " FAILED:" + failedPersonalFeeds + " SUCCESS:"
//				+ successfullPersonalFeeds);
//	}
	
//	private void collectTwitterHashTags() {
//		Collection<User> users = userManager.getAllUsers();
//		int failedPersonalFeeds = 0;
//		int successfullPersonalFeeds = 0;
//		
//		for (User user : users) {
//			if (!user.isSystem()) {
//				boolean success = feedsAgregator.aggregateTwitterHashTagsForUser(user);
//				
//				if (success) {
//					successfullPersonalFeeds++;
//				} else {
//					failedPersonalFeeds++;
//				}
//				
//				logger.info("Update daily digest with Hashtags for user:" + user.getId() + " lastname:" + user.getLastname() + " firstname:"
//						+ user.getName() + " success:" + success);
//			}
//		}
//		
//		logger.info("Create Daily Diggest Twitter HashTags FINISHED FOR:" + users.size() + " FAILED:" + failedPersonalFeeds + " SUCCESS:"
//				+ successfullPersonalFeeds);
//	}
	
//	private void createFriendsFeeds() {
//		Collection<User> users = userManager.getAllUsers();
//		int failedPersonalFeeds = 0;
//		int successfullPersonalFeeds = 0;
//		
//		for (User user : users) {
//			if (!user.isSystem()) {
//				user = userManager.merge(user);
//				boolean success = false;
//			
//				try {
//					success = feedsAgregator.aggregateFriendsFeedsForUser(user);
//				} catch (Exception ex) {
//					ex.getStackTrace();
//				}
//			
//				if (success) {
//					successfullPersonalFeeds++;
//				} else {
//					failedPersonalFeeds++;
//				}
//				
//				logger.info("Update daily digest with Friend feed for user:" + user.getId() + " lastname:" + user.getLastname() + " firstname:"
//						+ user.getName() + " success:" + success);
//			}
//		}
//		
//		logger.info("Create Daily Diggest Friends Feeds FINISHED FOR:" + users.size() + " FAILED:" + failedPersonalFeeds + " SUCCESS:"
//				+ successfullPersonalFeeds);
//	}
 }
