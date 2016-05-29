package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserNotificationsSettings;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.core.spring.security.UserSessionDataLoader;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.filters.LearningGoalFilter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.interfaceSettings.NotificationsSettingsManager;
import org.prosolo.services.logging.LoggingService;
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
	@Inject
	private EventFactory eventFactory;

	private SessionData sessionData;
	
	private LearningContext learningContext;
	
	public LoggedUserBean(){
		System.out.println("SESSION BEAN INITIALIZED");
		learningContext = new LearningContext();
	}
	
	private boolean initialized = false;
	
	public void initializeSessionData() {
		FacesContext currentInstance = FacesContext.getCurrentInstance();
		HttpSession session = null;
		if(currentInstance != null){
			logger.debug("initializeSession data started");
			session = (HttpSession) currentInstance.getExternalContext().getSession(false);
			initializeSessionData(session);
			logger.debug("initializeSession data finished");
		}
		
	}
	public void initializeSessionData(HttpSession session) {
		logger.debug("initializeSession data for session");
		@SuppressWarnings("unchecked")
		Map<String, Object> sData = (Map<String, Object>) session.getAttribute("user");
		initializeData(sData);
		sData = null;
		session.removeAttribute("user");
	}
	
	@SuppressWarnings("unchecked")
	private void initializeData(Map<String, Object> userData) {
		if(userData != null){
			sessionData = new SessionData();
			sessionData.setUser((User) userData.get("user"));
			sessionData.setBigAvatar((String) userData.get("avatar")); 
			sessionData.setPagesTutorialPlayed((Set<String>) userData.get("pagesTutorialPlayed"));
			sessionData.setIpAddress((String) userData.get("ipAddress"));
			sessionData.setSelectedStatusWallFilter((Filter) userData.get("statusWallFilter"));
			sessionData.setUserSettings((UserSettings) userData.get("userSettings"));
			sessionData.setNotificationsSettings((UserNotificationsSettings) userData.get("notificationsSettings"));
			sessionData.setEmail((String) userData.get("email"));
			sessionData.setFullName(setFullName(sessionData.getUser().getName(), 
					sessionData.getUser().getLastname()));
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
	
	public String setFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
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
		setNotificationsSettings(notificationsSettingsManager.getOrCreateNotificationsSettings(getUser().getId()));
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
			//loggingService.logEvent(EventType.LOGOUT, user, ipAddress);

			userManager.fullCacheClear();
			user = getUser();
			if (user != null) {
				//previously logEvent method was called
				//loggingService.logSessionEnded(EventType.SESSIONENDED, user, getIpAddress());
				try {
					Map<String, String> parameters = new HashMap<>();
					parameters.put("ip", ipAddress);
					eventFactory.generateEvent(EventType.SESSIONENDED, user, null, parameters);
				} catch (EventException e) {
					logger.error("Generate event failed.", e);
				}
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

		@SuppressWarnings("unchecked")
		List<GrantedAuthority> capabilities = (List<GrantedAuthority>) authentication.getAuthorities();

		if (capabilities == null) {
			return false;
		}

		for (GrantedAuthority auth : capabilities) {
			if (auth.getAuthority().toUpperCase().equals(capability.toUpperCase())) {
				return true;
			}
		}

		return false;
	}
	
	public void userLogout(){
		try {
			final String ipAddress = this.getIpAddress();
			User user = getUser();
			loggingService.logEvent(EventType.LOGOUT, user, ipAddress);
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
					.getExternalContext().getRequest();
			String contextP = req.getContextPath() == "/" ? "" : req.getContextPath();
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextP + "/logout");
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public SessionData getSessionData() {
		logger.debug("getSession data:initialized:"+initialized+" is sessionData null:"+sessionData==null);
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
	public LearningContext getLearningContext() {
		return learningContext;
	}
	public void setLearningContext(LearningContext learningContext) {
		this.learningContext = learningContext;
	}
	public String getFullName() {
		return getSessionData() == null ? null : getSessionData().getFullName();
	}
}