package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.core.spring.security.UserSessionDataLoader;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.filters.AllFilter;
import org.prosolo.services.activityWall.filters.AllProsoloFilter;
import org.prosolo.services.activityWall.filters.CourseFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.LearningGoalFilter;
import org.prosolo.services.activityWall.filters.MyActivitiesFilter;
import org.prosolo.services.activityWall.filters.MyNetworkFilter;
import org.prosolo.services.activityWall.filters.TwitterFilter;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.logging.AccessResolver;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.sessiondata.SessionData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@ManagedBean(name = "loggeduser")
@Component("loggeduser")
@Scope(value = "session")
public class LoggedUserBean implements Serializable, HttpSessionBindingListener {

	private static final long serialVersionUID = 1404040093737456717L;

	private static Logger logger = Logger.getLogger(LoggedUserBean.class);

	@Autowired
	private UserManager userManager;
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private InterfaceSettingsManager interfaceSettingsManager;
	@Autowired
	private NotificationsSettingsManager notificationsSettingsManager;
	@Autowired
	private ApplicationBean applicationBean;
	@Autowired
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private LoggingService loggingService;
	@Inject
	private UserSessionDataLoader sessionDataLoader;

	@Autowired
	private LearningGoalManager learningGoalManager;

	private SessionData sessionData;
	
	public LoggedUserBean(){
		System.out.println("SESSION BEAN INITIALIZED");
	}
	
	private boolean initialized = false;
	
	public void initializeSessionData() {
		FacesContext currentInstance = FacesContext.getCurrentInstance();
		HttpSession session = null;
		if(currentInstance != null){
			session = (HttpSession) currentInstance.getExternalContext().getSession(false);
			initializeSessionData(session);
		}
		
	}
	public void initializeSessionData(HttpSession session) {
		sessionData = new SessionData();
		Map<String, Object> sData = (Map<String, Object>) session.getAttribute("user");
		initializeData(sData);
		sData = null;
		session.removeAttribute("user");
	}
	private void initializeData(Map<String, Object> userData) {
		if(userData != null){
			sessionData.setUser((User) userData.get("user"));
			sessionData.setLoginTime((long) userData.get("loginTime"));
			sessionData.setBigAvatar((String) userData.get("avatar")); 
			sessionData.setPagesTutorialPlayed((Set<String>) userData.get("pagesTutorialPlayed"));
			sessionData.setIpAddress((String) userData.get("ipAddress"));
			sessionData.setSelectedStatusWallFilter((Filter) userData.get("statusWallFilter"));
			sessionData.setUserSettings((UserSettings) userData.get("userSettings"));
			sessionData.setNotificationsSettings((UserNotificationsSettings) userData.get("notificationsSettings"));
			initialized = true;
		}
	}
	
	public void init(String email) {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) ec.getSession(false);
		HttpServletRequest request = (HttpServletRequest) ec.getRequest();
		Map<String, Object> userData = sessionDataLoader.init(email, request, session);
		initializeData(userData);
	}
	
	public void initializeAvatar() {
		setBigAvatar(AvatarUtils.getAvatarUrlInFormat(getUser().getAvatarUrl(), ImageFormat.size120x120));
	}

	public boolean isLoggedIn() {
		return getUser() != null;
	}

	public User refreshUser() {
		setUser(userManager.merge(getUser()));
		refreshUserSettings();
		return getUser();
	}
	
	public void loadStatusWallFilter(FilterType chosenFilterType, long courseId) {
		Filter filter = sessionDataLoader.loadStatusWallFilter(getUser(), chosenFilterType, courseId);
		setSelectedStatusWallFilter(filter);
	}

	public void loadGoalWallFilter(long targetLearningGoal) {
		LearningGoalFilter filter = new LearningGoalFilter(targetLearningGoal);
		Long goalId = learningGoalManager.getGoalIdForTargetGoal(targetLearningGoal);
		filter.setGoalId(goalId);
		Set<Long> collaborators = learningGoalManager.getCollaboratorsIdsByTargetGoalId(targetLearningGoal);
		filter.setCollaborators(collaborators);
		Set<Long> tComps = learningGoalManager.getTargetCompetencesForTargetLearningGoal(targetLearningGoal);
		filter.setTargetCompetences(tComps);
		Set<Long> tActivities = learningGoalManager.getTargetActivitiesForTargetLearningGoal(targetLearningGoal);
		filter.setTargetActivities(tActivities);

		setSelectedLearningGoalFilter(filter);
	}

	public void refreshUserSettings() {
		setUserSettings(interfaceSettingsManager.getOrCreateUserSettings(getUser()));
	}

	public void refreshNotificationsSettings() {
		setNotificationsSettings(notificationsSettingsManager.getOrCreateNotificationsSettings(getUser()));
	}

	public void loginOpenId(String email) {
		boolean loggedIn;
		try {
			loggedIn = authenticationService.loginOpenId(email);

			if (loggedIn) {
				logger.info("LOGGED IN:" + email);
				setEmail(email);
				
				
				init(email);
				
				logger.info("Initialized");
				//change --
				//ipAddress = accessResolver.findRemoteIPAddress();
				logger.info("LOGING EVENT");
				// this.checkIpAddress();
				loggingService.logEvent(EventType.LOGIN, getUser(), getIpAddress());
				// return "index?faces-redirect=true";
				logger.info("REDIRECTING TO INDEX");
				
				HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
						.getExternalContext().getRequest();
				String contextP = req.getContextPath() == "/" ? "" : req.getContextPath();
				FacesContext.getCurrentInstance().getExternalContext().redirect(contextP + new HomePageResolver().getHomeUrl());
				return;
			}
		} catch (org.prosolo.services.authentication.exceptions.AuthenticationException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/login?faces-redirect=true");
			return;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		User user = null;
		if(initialized){
			final String ipAddress = this.getIpAddress();
			loggingService.logEvent(EventType.LOGOUT, user, ipAddress);

			userManager.fullCacheClear();
			user = getUser();
			if (user != null) {
				loggingService.logEvent(EventType.SESSIONENDED, user, getIpAddress());
				userManager.fullCacheClear();
				logger.debug("UserSession unbound:" + event.getName() + " session:" + event.getSession().getId()
						+ " for user:" + user.getId());
			}
		}
		// else{
		// logger.debug("UserSession unbound:" + event.getName() + " session:" +
		// event.getSession().getId());
		// }
		applicationBean.unregisterSession(event.getSession());
		applicationBean.unregisterUser(user);
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// logger.debug("UserSession bound");
	}

	public boolean toPlayTutorial(String page) {
		if (page != null) {
			return !getSessionData().getPagesTutorialPlayed().contains(page);
		}
		return false;
	}

	public void tutorialPlayed(boolean playButtonPressed, final String page) {
		if (playButtonPressed || isDoNotShowTutorial()) {
			getSessionData().getPagesTutorialPlayed().add(page);

			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Session session = (Session) userManager.getPersistence().openSession();

					try {
						setUserSettings(interfaceSettingsManager.tutorialsPlayed(getUser().getId(), page, session));
						session.flush();
					} catch (Exception e) {
						logger.error("Exception in handling message", e);
					} finally {
						HibernateUtil.close(session);
					}
				}
			});
		}
		setDoNotShowTutorial(false);
	}

	public boolean hasCapability(String capability) {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return false;
		}

		Authentication authentication = context.getAuthentication();
		if (authentication == null) {
			return false;
		}

		List<GrantedAuthority> capabilities = (List<GrantedAuthority>) authentication.getAuthorities();

		if (capabilities == null) {
			return false;
		}

		for (GrantedAuthority auth : capabilities) {
			if (capability.toUpperCase().equals(auth.getAuthority()))
				return true;
		}

		return false;
	}
	
	public void userLogout(){
		try {
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
					.getExternalContext().getRequest();
			String contextP = req.getContextPath() == "/" ? "" : req.getContextPath();
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextP + "/logout");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public SessionData getSessionData() {
		if(!initialized){
			initializeSessionData();
		}
		return sessionData;
	}

	public void setSessionData(SessionData sessionData) {
		this.sessionData = sessionData;
	}
	
	public String getEmail() {
		return getSessionData() == null ? null : getSessionData().getEmail();
	}

	public void setEmail(String email) {
		getSessionData().setEmail(email);
	}

	public String getPassword() {
		return getSessionData() == null ? null : getSessionData().getPassword();
	}

	public void setPassword(String password) {
		getSessionData().setPassword(password);
	}

	public User getUser() {
		return getSessionData() == null ? null : getSessionData().getUser();
	}

	public void setUser(User user) {
		getSessionData().setUser(user);
	}

	public String getName() {
		return getSessionData() == null ? null : getUser().getName();
	}

	public String getLastName() {
		return getSessionData() == null ? null : getUser().getLastname();
	}

	public UserSettings getInterfaceSettings() {
		return getSessionData() == null ? null : getSessionData().getUserSettings();
	}

	public Locale getLocale() {
		if (isLoggedIn() && getInterfaceSettings() != null && getInterfaceSettings().getLocaleSettings() != null) {
			return getInterfaceSettings().getLocaleSettings().createLocale();
		} else {
			return new Locale("en", "US");
		}
	}

	public long getLoginTime() {
		return getSessionData() == null ? null : getSessionData().getLoginTime();
	}

	public void setLoginTime(long loginTime) {
		getSessionData().setLoginTime(loginTime);
	}

	public String getBigAvatar() {
		return getSessionData() == null ? null : getSessionData().getBigAvatar();
	}

	public void setBigAvatar(String bigAvatar) {
		getSessionData().setBigAvatar(bigAvatar);
	}

	public boolean isDoNotShowTutorial() {
		return getSessionData() == null ? null : getSessionData().isDoNotShowTutorial();
	}

	public void setDoNotShowTutorial(boolean doNotShowTutorial) {
		getSessionData().setDoNotShowTutorial(doNotShowTutorial);
	}

	public Filter getSelectedStatusWallFilter() {
		return getSessionData() == null ? null : getSessionData().getSelectedStatusWallFilter();
	}

	public void setSelectedStatusWallFilter(Filter selectedStatusWallFilter) {
		getSessionData().setSelectedStatusWallFilter(selectedStatusWallFilter);
	}

	public LearningGoalFilter getSelectedLearningGoalFilter() {
		return getSessionData() == null ? null : getSessionData().getSelectedLearningGoalFilter();
	}

	public void setSelectedLearningGoalFilter(LearningGoalFilter selectedLearningGoalFilter) {
		getSessionData().setSelectedLearningGoalFilter(selectedLearningGoalFilter);
	}

	public UserNotificationsSettings getNotificationsSettings() {
		return getSessionData() == null ? null : getSessionData().getNotificationsSettings();
	}

	public void setNotificationsSettings(UserNotificationsSettings notificationsSettings) {
		getSessionData().setNotificationsSettings(notificationsSettings);
	}
	public String getIpAddress() {
		return getSessionData() == null ? null : getSessionData().getIpAddress();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public UserSettings getUserSettings() {
		return getSessionData() == null ? null : getSessionData().getInterfaceSettings();
	}
	
	public void setUserSettings(UserSettings userSettings) {
		getSessionData().setUserSettings(userSettings);
	}
}