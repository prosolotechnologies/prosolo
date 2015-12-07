package org.prosolo.core.spring.security;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.filters.AllFilter;
import org.prosolo.services.activityWall.filters.AllProsoloFilter;
import org.prosolo.services.activityWall.filters.CourseFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.MyActivitiesFilter;
import org.prosolo.services.activityWall.filters.MyNetworkFilter;
import org.prosolo.services.activityWall.filters.TwitterFilter;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.SessionCountBean;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSessionDataLoader implements Serializable{

	private static final long serialVersionUID = 8483368624265629491L;

	private static Logger logger = Logger.getLogger(UserSessionDataLoader.class);
	
	@Inject
	private UserManager userManager;
	@Autowired
	private NotificationsSettingsManager notificationsSettingsManager;
	@Autowired
	private ApplicationBean applicationBean;
	@Autowired
	private SessionCountBean sessionCounter;
	@Autowired
	private ActivityWallManager activityWallManager;
	@Autowired
	private TagManager tagManager;
	@Autowired
	private CourseManager courseManager;
	@Autowired
	private InterfaceSettingsManager interfaceSettingsManager;
	@Autowired
	private AccessResolver accessResolver;
	
	
	//private User user;
	//private long loginTime;
	//private String bigAvatar;
	//private Set<String> pagesTutorialPlayed = new HashSet<String>();
	//private String ipAddress;
	//private Filter selectedStatusWallFilter;
	//private UserSettings userSettings;
	//private UserNotificationsSettings notificationsSettings;
	
	public Map<String, Object> init(String email, HttpServletRequest request, HttpSession session) throws SessionInitializationException{
		try{
			logger.info("login user with email "+email);
			Map<String, Object> sessionData = new HashMap<>();
			
			User user = userManager.getUser(email);
			long loginTime = new Date().getTime();
			String avatar = initializeAvatar(user.getAvatarUrl());
			
			registerNewUserSession(user, session);
			
			UserSettings userSettings = interfaceSettingsManager.getOrCreateUserSettings(user);
			UserNotificationsSettings notificationsSettings = notificationsSettingsManager.getOrCreateNotificationsSettings(user);
	
			FilterType chosenFilterType = userSettings.getActivityWallSettings().getChosenFilter();
	
			Filter selectedFilter = loadStatusWallFilter(user, chosenFilterType, userSettings.getActivityWallSettings().getCourseId());
	
			Set<String> pagesTutorialPlayed = userSettings.getPagesTutorialPlayed();
			
			String ipAddress = accessResolver.findRemoteIPAddress(request);
			logger.debug("User \"" + email + "\" IP address:" + ipAddress);
			
			sessionData.put("user", user);
			sessionData.put("loginTime", loginTime);
			sessionData.put("avatar", avatar);
			sessionData.put("pagesTutorialPlayed", pagesTutorialPlayed);
			sessionData.put("ipAddress", ipAddress);
			sessionData.put("statusWallFilter", selectedFilter);
			sessionData.put("userSettings", userSettings);
			sessionData.put("notificationsSettings", notificationsSettings);
			
			logger.info("init finished");
			return sessionData;
		}catch(Exception e){
			logger.error(e);
			e.printStackTrace();
			throw new SessionInitializationException();
		}
	}
	
	private String initializeAvatar(String avatarUrl) {
		return AvatarUtils.getAvatarUrlInFormat(avatarUrl, ImageFormat.size120x120);
	}

	private void registerNewUserSession(User user, HttpSession session) {
		applicationBean.registerNewUserSession(user, session);
		sessionCounter.addSession(session.getId());
	}
	
	public Filter loadStatusWallFilter(User user, FilterType chosenFilterType, long courseId) {
		Filter selectedStatusWallFilter = null;
		if (chosenFilterType.equals(FilterType.MY_NETWORK)) {
			selectedStatusWallFilter = new MyNetworkFilter();
			Set<Long> myNetworkUsers = activityWallManager.getUsersInMyNetwork(user.getId());
			((MyNetworkFilter) selectedStatusWallFilter).setUserIds(myNetworkUsers);
		} else if (chosenFilterType.equals(FilterType.MY_ACTIVITIES)) {
			selectedStatusWallFilter = new MyActivitiesFilter();
		} else if (chosenFilterType.equals(FilterType.ALL)) {
			selectedStatusWallFilter = new AllFilter();
		} else if (chosenFilterType.equals(FilterType.TWITTER)) {
			TwitterFilter twitterFilter = new TwitterFilter();
			twitterFilter.setHashtags(new TreeSet<Tag>(tagManager.getSubscribedHashtags(user)));
			selectedStatusWallFilter = twitterFilter;
		} else if (chosenFilterType.equals(FilterType.ALL_PROSOLO)) {
			selectedStatusWallFilter = new AllProsoloFilter();
		} else if (chosenFilterType.equals(FilterType.COURSE)) {
			CourseFilter courseFilter = new CourseFilter();
			try {
				Course course = userManager.loadResource(Course.class, courseId);
				courseFilter.setCourseId(courseId);
				Map<String, Set<Long>> goalTargetGoals = courseManager.getTargetLearningGoalIdsForCourse(course);
				// Long
				// targetLearningGoalId=courseManager.getTargetLearningGoalIdForCourse(user,
				// course);
				courseFilter.setTargetLearningGoals(goalTargetGoals.get("targetGoals"));
				// Long
				// goalId=learningGoalManager.getGoalIdForTargetGoal(targetLearningGoalId);
				courseFilter.setLearningGoals(goalTargetGoals.get("goals"));
				Set<Long> tComps = courseManager.getTargetCompetencesForCourse(course);
				courseFilter.setTargetCompetences(tComps);
				Set<Long> tActivities = courseManager.getTargetActivitiesForCourse(course);
				courseFilter.setTargetActivities(tActivities);
				/*
				 * Set<Long>
				 * tComps=competenceManager.getTargetCompetencesIds(user.getId()
				 * , goalId); courseFilter.setTargetCompetences(tComps);
				 * Set<Long> targetActivities=new TreeSet<Long>(); for(Long
				 * tc:tComps){ Set<Long>
				 * ta=competenceManager.getTargetActivities(tc);
				 * targetActivities.addAll(ta); }
				 * courseFilter.setTargetActivities(targetActivities);
				 */

				Set<Tag> hashtags = course.getHashtags();
				for (Tag tag : hashtags) {
					courseFilter.addHashtag(tag.getTitle());
				}
				// courseFilter.setHashtags(course.getHashtags());
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			selectedStatusWallFilter = courseFilter;
		}
		
		return selectedStatusWallFilter;
	}

}
