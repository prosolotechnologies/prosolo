package org.prosolo.web.activitywall.displayers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.activitywall.util.ActivityWallUtils;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public abstract class DefaultWallSocialActivitiesDisplayer {
	
	@Autowired protected ActivityWallManager activityWallManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private Logger logger = Logger.getLogger(DefaultWallSocialActivitiesDisplayer.class);
	
	private List<SocialActivityData> allActivities;
	private List<SocialActivityData> newActivities = new ArrayList<SocialActivityData>();
	private int limit = 7;
	private boolean moreToLoad = false;
	private long lastActivityToDisplayId = 0;
	protected User loggedUser;
	protected Locale locale;
	protected Filter filter;
	
	// stores user's scroll position on the page so we could know when we can safely 
	// remove some activities from the status wall without affecting the UI
	private long scrollPosition;
	
	protected abstract List<SocialActivityData> fetchActivities(int offset, int limit); 
	protected abstract SocialStreamSubViewType getSubViewType();
	
	public void init(User loggedUser, Locale locale, Filter filter) {
		this.loggedUser = loggedUser;
		this.locale = locale;
		this.filter = filter;
	}
	
	public void initializeActivities() {
		allActivities = null;
		
		// if activities are not initialized
//		if (allActivities == null) {
			logger.debug("loadActivities in " + this);
			
			// fetch those activities and add them to the list
			allActivities = loadActivities(0, limit);
			
 			// if the list has any activities, return the latest one
			if (!allActivities.isEmpty()) {
				lastActivityToDisplayId = allActivities.get(allActivities.size()-1).getSocialActivity().getId();
			} else {
				lastActivityToDisplayId = 0;
			}
//		} else {
//			addWallActivities(loadActivities(allActivities.size(), limit));
//			
//			if (!allActivities.isEmpty())
//				lastActivityToDisplayId =  allActivities.get(allActivities.size()-1).getSocialActivity().getId();
//		}
	}
	
	public void resetActivities() {
		this.allActivities = null;
	}
	
	private List<SocialActivityData> loadActivities(int offset, int limit) {
		List<SocialActivityData> socialActivities = fetchActivities(offset, limit);
		
		// if there is more than limit, set moreToLoad to true
		if (socialActivities.size() == limit+1) {
			socialActivities = socialActivities.subList(0, socialActivities.size()-1);
			moreToLoad = true;
		} else {
			moreToLoad = false;
		}
		
		List<SocialActivityData> socialActivitiesData = wallActivityConverter.initializeSocialActivitiesData(
				socialActivities, 
				loggedUser,
				SocialStreamSubViewType.STATUS_WALL,
				locale,
				true);
		
		return socialActivitiesData;
	}

	public void loadMoreActivities(String context) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		loadMoreActivities(parameters);
	}
	
	public void loadMoreActivities(final Map<String, String> parameters) {
		lastActivityToDisplayId = loadMoreStartingFromActivity(limit, lastActivityToDisplayId);
		
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				actionLogger.logEvent(EventType.SERVICEUSE, "MOUSE_CLICK", parameters);
			}
		});
	}
	
	private long loadMoreStartingFromActivity(int limit, long startingActivityId) {
		int index = ActivityWallUtils.getIndexOfSocialActivity(allActivities, startingActivityId);
		
		List<SocialActivityData> fetchedActivities = loadActivities(index+1, limit);
		addWallActivities(fetchedActivities);
		
		if (fetchedActivities != null && !fetchedActivities.isEmpty()) {
			return fetchedActivities.get(fetchedActivities.size()-1).getSocialActivity().getId();
		}
		else {
			return startingActivityId;
		}
	}
	
	public synchronized void refresh() {
		if (!newActivities.isEmpty()) {
			allActivities.addAll(0, newActivities);
			newActivities.clear();
			
			/* 
			 * if there are more than 20 activities and user is looking at the top of the page, 
			 * remove extra activities from the list
			 */
			if (allActivities.size() > 20 && scrollPosition < 1500) {
				Iterator<SocialActivityData> iterator = allActivities.iterator();
				int index = 1;
				
				while (iterator.hasNext()) {
					iterator.next();
					
					if (index > 20) {
						iterator.remove();
					}
					index++;
				}
			}
		}
	}
	
	public synchronized void addWallActivities(List<SocialActivityData> socialActivities) {
		if (socialActivities != null && allActivities != null) {
			allActivities.addAll(socialActivities);
		}
	}
	
	public synchronized void addNewWallActivity(SocialActivityData socialActivityData) {
		if (socialActivityData != null && allActivities != null) {
			newActivities.add(0, socialActivityData);
		}
	}
	
	public synchronized void addSocialActivity(SocialActivity socialActivity, User loggedUser, Locale locale, boolean optionsDisabled) {
		if (allActivities != null) {
			SocialActivityData wallActivity = wallActivityConverter.convertSocialActivityToSocialActivityData(socialActivity, loggedUser, getSubViewType(), locale);
//			wallActivity.setWallOwner(new UserData(loggedUser.getUser()));
//			wallActivity.setOptionsDisabled(true);
//			
			// allActivities can be null if index page is quickly skipped before allActivities was able to initialize
			if (allActivities != null) {
				newActivities.add(0, wallActivity);
			}
		}
	}
	
	public synchronized void removeSocialActivity(long socialActivityId) {
		if (allActivities != null) {
			Iterator<SocialActivityData> actIterator = allActivities.iterator();
			
			while (actIterator.hasNext()) {
				SocialActivityData socialActivityWallData = (SocialActivityData) actIterator.next();
				
				if (socialActivityWallData.getSocialActivity().getId() == socialActivityId) {
					actIterator.remove();
					break;
				}
			}
		}
	}
	
	public synchronized void updateSocialActivity(SocialActivity socialActivity) {
		if (allActivities != null) {
			Iterator<SocialActivityData> actIterator = allActivities.iterator();
			
			while (actIterator.hasNext()) {
				SocialActivityData socialActivityWallData  = (SocialActivityData) actIterator.next();
				
				if (socialActivityWallData.getSocialActivity().getId() == socialActivity.getId()) {
					socialActivityWallData.setLastAction(socialActivity.getLastAction());
					socialActivityWallData.setUpdated(socialActivity.isUpdated());
					socialActivityWallData.setText(socialActivity.getText());
					socialActivityWallData.setCommentsDisabled(socialActivity.isCommentsDisabled());
					socialActivityWallData.setLikeCount(socialActivity.getLikeCount());
					socialActivityWallData.setDislikeCount(socialActivity.getDislikeCount());
					socialActivityWallData.setShareCount(socialActivity.getShareCount());
					break;
				}
			}
		}
	}
	
	public void disableSharing(SocialActivity socialActivity) {
		if (allActivities != null) {
			Iterator<SocialActivityData> actIterator = allActivities.iterator();
			
			while (actIterator.hasNext()) {
				SocialActivityData socialActivityWallData  = (SocialActivityData) actIterator.next();
				
				if (socialActivityWallData.getSocialActivity().getId() == socialActivity.getId()) {
					socialActivityWallData.setShared(true);
					break;
				}
			}
		}
	}
	
	public synchronized void addCommentToSocialActivity(long socialActivityId, Comment comment) {
		Iterator<SocialActivityData> actIterator = allActivities.iterator();
		
		while (actIterator.hasNext()) {
			SocialActivityData socialActivityWallData = (SocialActivityData) actIterator.next();
			
			if (socialActivityWallData.getSocialActivity().getId() == socialActivityId) {
				
				// TODO: Nikola
//				ActivityCommentData commentData = new ActivityCommentData(comment, 0, false, socialActivityWallData);
//				socialActivityWallData.addComment(commentData);
				break;
			}
		}
	}
	
	@Deprecated
	public synchronized void addLikeToComment(SocialActivity socialActivity, Comment comment) {
		Iterator<SocialActivityData> actIterator = allActivities.iterator();
		
		while (actIterator.hasNext()) {
			SocialActivityData socialActivityWallData = (SocialActivityData) actIterator.next();
			
			if (socialActivityWallData.getSocialActivity().getId() == socialActivity.getId()) {
				List<SocialActivityCommentData> commentsData = socialActivityWallData.getComments();
				
				for (SocialActivityCommentData activityCommentData : commentsData) {
					if (activityCommentData.getId() == comment.getId()) {
						activityCommentData.setLikeCount(activityCommentData.getLikeCount()+1);
						return;
					}
				}
			}
		}
	}
	
	public synchronized void updateCommentDataOfSocialActivity(long socialActivityId, long commentId, int likeCount, int dislikeCount) {
		Iterator<SocialActivityData> actIterator = allActivities.iterator();
		
		while (actIterator.hasNext()) {
			SocialActivityData socialActivityWallData = (SocialActivityData) actIterator.next();
			
			if (socialActivityWallData.getSocialActivity().getId() == socialActivityId) {
				List<SocialActivityCommentData> commentsData = socialActivityWallData.getComments();
				
				for (SocialActivityCommentData activityCommentData : commentsData) {
					if (activityCommentData.getId() == commentId) {
						activityCommentData.setLikeCount(likeCount);
						activityCommentData.setDislikeCount(dislikeCount);
						return;
					}
				}
			}
		}
	}
	
	@Deprecated
	public void markSocialActivityWallDataAsUnfollowed(List<String> hashtags) {
		logger.debug("Marking as unfollowed social activity data instances having hashtags: "+hashtags);
		
		if (allActivities != null) {
			for (SocialActivityData activityData : allActivities) {
				if (activityData.getHashtags() != null) {

					hashtahLoop: for (String string : hashtags) {
						if (activityData.getHashtags().contains(string)) {
							activityData.setUnfollowedHashtags(true);
							break hashtahLoop;
						}
					}
				}
			}
		}
	}
	
	public void changeFilter(Filter selectedStatusWallFilter) {
		this.allActivities = null;
		this.moreToLoad = false;
		this.lastActivityToDisplayId = 0;
		this.filter = selectedStatusWallFilter;
		
		initializeActivities();
//		newActivities.clear();
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public List<SocialActivityData> getAllActivities() {
		return allActivities;
	}

	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public boolean isMoreToLoad() {
		return moreToLoad;
	}

	public long getScrollPosition() {
		return scrollPosition;
	}

	public void setScrollPosition(long scrollPosition) {
		this.scrollPosition = scrollPosition;
	}
	
}
