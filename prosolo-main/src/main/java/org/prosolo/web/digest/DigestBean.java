package org.prosolo.web.digest;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.feeds.FeedEntry;
import org.prosolo.common.domainmodel.user.TimeFrame;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.digest.FilterOption;
import org.prosolo.common.web.digest.data.FeedEntryData;
import org.prosolo.common.web.digest.data.FeedsDigestData;
import org.prosolo.services.feeds.FeedsManager;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.digest.data.DigestCriteria;
import org.prosolo.web.digest.data.FeedEntryDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic 2013-08-18
 *
 */
@ManagedBean(name = "digestBean1")
@Component("digestBean1")
@Scope("view")
public class DigestBean implements Serializable{

	private static final long serialVersionUID = -8628650301487013231L;

	protected static Logger logger = Logger.getLogger(DigestBean.class);

	@Autowired private LoggedUserBean loggedUser;

	@Autowired private FeedsManager feedsManager;
	@Autowired private CourseManager courseManager;
	
	private FeedsDigestData feedDigestData = new FeedsDigestData();
	private FilterOption filter;
	private int limit = 10;
	private TimeFrame interval = null;
	private long userId;
	private long courseId;
	private Date date;
	private List<SelectItem> courseOptions;
	
	private DigestCriteria criteria = new DigestCriteria();
	
	@PostConstruct
	public void setDefaults() {
		if (loggedUser != null && loggedUser.getUser() != null) {
			this.userId = loggedUser.getUser().getId();
			
			if (filter == null) {
				filter = FilterOption.myfeeds;
			}
		} else {
			if (filter == null) {
				filter = FilterOption.coursefeeds;
			}
		}
		
		// init courses
		courseOptions = new LinkedList<SelectItem>();
		
		Collection<Course> activeCourses =  courseManager.getAllActiveCourses();
		
		if (activeCourses != null && !activeCourses.isEmpty()) {
			int index = 0;
			
			for (Course course : activeCourses) {
				if (index == 0) {
					courseId = course.getId();
				}
				
				courseOptions.add(new SelectItem(course.getId(), course.getTitle(), null, false));
				index++;
			}
		}
	}

	public void fetchDailyDigests() {
		feedDigestData = new FeedsDigestData();
		
		this.date = DateUtil.parseDashDate(criteria.getDate());
		
		if (this.date == null) {
			this.date = new Date();
		}
		
		this.interval = null;
		
		if (criteria.getInterval() != null) {
			this.interval = TimeFrame.valueOf(criteria.getInterval().toUpperCase());
			
			if (this.interval == null) {
				this.interval = TimeFrame.DAILY;
			}
		} else {
			this.interval = TimeFrame.DAILY;
		}
		
		if (criteria.getFilter() != null && !criteria.getFilter().isEmpty()) {
			try {
				filter = FilterOption.valueOf(criteria.getFilter());
			}  catch (IllegalArgumentException e) {
				logger.debug("Could not convert " + criteria.getFilter() + " to a filed of enum FilterOption.");
			}
		}
		
		this.userId = 0;
		
		setDefaults();
		
		collectFeedsForFilter();
		
//		Map<FilterOption, List<FeedEntry>> digests = feedsManager.filterAllDigests(userId, 1, date, filters, interval, limit, page);
//		
//		if (digests != null && !digests.isEmpty()) {
//			for (Entry<FilterOption, List<FeedEntry>> feedsDigest : digests.entrySet()) {
//				FilterOption filterOption = FeedsUtil.convertToFilterOption(feedsDigest.getClass().getSimpleName());
//				
//				allFeeds.put(filterOption, new FeedsDigestData(feedsDigest));
//			}
//		}
	}

	@SuppressWarnings("incomplete-switch")
	public void collectFeedsForFilter() {
		
		Date dateFrom = null;
		Date dateTo = null;
		
		switch (interval) {
			case DAILY:
				dateFrom = DateUtil.getDayBeginningDateTime(date);
				dateTo = DateUtil.getNextDay(date);
				break;
			case WEEKLY:
				dateFrom = DateUtil.getWeekBeginningDate(date);
				dateTo = DateUtil.getNextWeekBeginningDate(date);
				break;
			case MONTHLY:
				dateFrom = DateUtil.getMonthBeginningDate(date);
				dateTo = DateUtil.getNextMonthBeginningDate(date);
				break;
		}
		
		switch (filter) {
			case myfeeds:
				List<FeedEntry> entries = feedsManager.getMyFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedDigestData.getPage());
				
				addFeedEntries(feedDigestData, entries, limit);
				break;
			case friendsfeeds:
				List<FeedEntry> entries1 = feedsManager.getMyFriendsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedDigestData.getPage());
				
				addFeedEntries(feedDigestData, entries1, limit);
				break;
			case mytweets:
				List<TwitterPostSocialActivity> entries2 = feedsManager.getMyTweetsFeedsDigest(userId, dateFrom, dateTo, interval, limit, feedDigestData.getPage());
				
				addTweetEntries(feedDigestData, entries2, limit);
				break;
			case coursefeeds:
				List<FeedEntry> entries3 = feedsManager.getCourseFeedsDigest(courseId, dateFrom, dateTo, interval, limit, feedDigestData.getPage());
				
				addFeedEntries(feedDigestData, entries3, limit);
				break;
			case coursetweets:
				List<TwitterPostSocialActivity> entries4 = feedsManager.getCourseTweetsDigest(courseId, dateFrom, dateTo, interval, limit, feedDigestData.getPage());
				
				addTweetEntries(feedDigestData, entries4, limit);
				break;
		}
	}

	public static void addFeedEntries(FeedsDigestData feedsDigestData, List<FeedEntry> entries, int limit) {
		// if there is more than limit, set moreToLoad to true
		if (entries.size() == limit + 1) {
			entries = entries.subList(0, entries.size() - 1);
			feedsDigestData.setMoreToLoad(true);
		} else {
			feedsDigestData.setMoreToLoad(false);
		}
		
		for (FeedEntry feedEntry : entries) {
			feedsDigestData.addEntry(new FeedEntryData(feedEntry));
		}
	}
	
	public static void addTweetEntries(FeedsDigestData feedsDigestData, List<TwitterPostSocialActivity> entries, int limit) {
		// if there is more than limit, set moreToLoad to true
		if (entries.size() == limit + 1) {
			entries = entries.subList(0, entries.size() - 1);
			feedsDigestData.setMoreToLoad(true);
		} else {
			feedsDigestData.setMoreToLoad(false);
		}
		
		for (TwitterPostSocialActivity tweet : entries) {
			feedsDigestData.addEntry(FeedEntryDataFactory.createFeedEntryData(tweet));
		}
	}
	
	public void loadMore() {
		// Incrementing page
		feedDigestData.incrementPage();
		
		collectFeedsForFilter();
	}

	/* 
	 * GETTERS / SETTERS 
	 */
	public DigestCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(DigestCriteria criteria) {
		this.criteria = criteria;
	}

	public FilterOption getFilter() {
		return filter;
	}

	public FeedsDigestData getFeedDigestData() {
		return feedDigestData;
	}

	public List<SelectItem> getCourseOptions() {
		return courseOptions;
	}

	public long getCourseId() {
		return courseId;
	}

	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
}
