package org.prosolo.core.spring.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.spring.security.exceptions.SessionInitializationException;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.filters.AllFilter;
import org.prosolo.services.activityWall.filters.AllProsoloFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.MyActivitiesFilter;
import org.prosolo.services.activityWall.filters.MyNetworkFilter;
import org.prosolo.services.activityWall.filters.TwitterFilter;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.SessionCountBean;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class UserSessionDataLoader implements Serializable{

	private static final long serialVersionUID = 8483368624265629491L;

	private static Logger logger = Logger.getLogger(UserSessionDataLoader.class);
	
	@Inject
	private UserManager userManager;
	@Inject
	private ApplicationBean applicationBean;
	@Inject
	private SessionCountBean sessionCounter;
	@Inject
	private SocialActivityManager socialActivityManager;
	@Inject
	private TagManager tagManager;
	@Inject
	private InterfaceSettingsManager interfaceSettingsManager;
	@Inject
	private NotificationsSettingsManager notificationsSettingsManager;
	@Inject
	private AccessResolver accessResolver;
	
	public Map<String, Object> init(String email, HttpServletRequest request, HttpSession session) throws SessionInitializationException{
		try{
			logger.info("login user with email "+email);
			Map<String, Object> sessionData = new HashMap<>();
			
			User user = userManager.getUser(email);
			String avatar = initializeAvatar(user.getAvatarUrl());
			
			registerNewUserSession(user, session);

			
			UserSettings userSettings = interfaceSettingsManager.getOrCreateUserSettings(user.getId());
	
			FilterType chosenFilterType = userSettings.getActivityWallSettings().getChosenFilter();
			
			Filter selectedFilter = loadStatusWallFilter(user.getId(), chosenFilterType, userSettings.getActivityWallSettings().getCourseId());
	
			String ipAddress = accessResolver.findRemoteIPAddress(request);
			logger.debug("User \"" + email + "\" IP address:" + ipAddress);
			
			UserNotificationsSettings notificationsSettings  = notificationsSettingsManager.getOrCreateNotificationsSettings(user.getId());
			
			sessionData.put("userId", user.getId());
			sessionData.put("name", user.getName());
			sessionData.put("lastname", user.getLastname());
			sessionData.put("avatar", avatar);
			sessionData.put("position", user.getPosition());
			sessionData.put("ipAddress", ipAddress);
			sessionData.put("statusWallFilter", selectedFilter);
			sessionData.put("userSettings", userSettings);
			sessionData.put("email", email);
			sessionData.put("notificationsSettings", notificationsSettings);
			sessionData.put("password", user.getPassword());
			sessionData.put("sessionId",session.getId());
			
			logger.info("init finished");
			return sessionData;
		} catch (Exception e) {
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
	
	public Filter loadStatusWallFilter(long userId, FilterType chosenFilterType, long courseId) {
		Filter selectedStatusWallFilter = null;
		if (chosenFilterType.equals(FilterType.MY_NETWORK)) {
			selectedStatusWallFilter = new MyNetworkFilter();
			Set<Long> myNetworkUsers = socialActivityManager.getUsersInMyNetwork(userId);
			((MyNetworkFilter) selectedStatusWallFilter).setUserIds(myNetworkUsers);
		} else if (chosenFilterType.equals(FilterType.MY_ACTIVITIES)) {
			selectedStatusWallFilter = new MyActivitiesFilter();
		} else if (chosenFilterType.equals(FilterType.ALL)) {
			selectedStatusWallFilter = new AllFilter();
		} else if (chosenFilterType.equals(FilterType.TWITTER)) {
			TwitterFilter twitterFilter = new TwitterFilter();
			twitterFilter.setHashtags(new TreeSet<Tag>(tagManager.getSubscribedHashtags(userId)));
			selectedStatusWallFilter = twitterFilter;
		} else if (chosenFilterType.equals(FilterType.ALL_PROSOLO)) {
			selectedStatusWallFilter = new AllProsoloFilter();
		} 
		
		return selectedStatusWallFilter;
	}
}
