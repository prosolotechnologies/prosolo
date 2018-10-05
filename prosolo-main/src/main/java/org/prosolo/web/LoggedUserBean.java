package org.prosolo.web;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.common.event.context.LearningContext;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.security.HomePageResolver;
import org.prosolo.core.spring.security.UserSessionDataLoader;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.authentication.AuthenticationService;
import org.prosolo.services.authentication.exceptions.AuthenticationException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interfaceSettings.InterfaceSettingsManager;
import org.prosolo.services.logging.LoggingService;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.sessiondata.SessionData;
import org.prosolo.web.settings.data.NotificationSettingsData;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Component;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ManagedBean(name = "loggeduser")
@Component("loggeduser")
@Scope(value = "session")
public class LoggedUserBean implements Serializable, HttpSessionBindingListener {

	private static final long serialVersionUID = 1404040093737456717L;

	private static Logger logger = Logger.getLogger(LoggedUserBean.class);

	@Inject
	private UserManager userManager;
	@Inject
	private AuthenticationService authenticationService;
	@Inject
	private InterfaceSettingsManager interfaceSettingsManager;
	@Inject
	private ApplicationBean applicationBean;
	@Inject
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private LoggingService loggingService;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private UserSessionDataLoader sessionDataLoader;

//	@Inject
//	private LearningGoalManager learningGoalManager;
	@Inject
	private EventFactory eventFactory;

	private SessionData sessionData;
	
	private LearningContext learningContext;

	private UserData loginAsUser;
	
	public LoggedUserBean(){
		learningContext = new LearningContext();
	}
	
	private boolean initialized = false;
	
	public void initializeSessionData() {
		FacesContext currentInstance = FacesContext.getCurrentInstance();
		HttpSession session = null;
		if (currentInstance != null) {
			session = (HttpSession) currentInstance.getExternalContext().getSession(false);
			initializeSessionData(session);
		}
	}
	
	public void initializeSessionData(HttpSession session) {
		@SuppressWarnings("unchecked")
		Map<String, Object> sData = (Map<String, Object>) session.getAttribute("user");
		initializeData(sData);
		sData = null;
		session.removeAttribute("user");
	}
	
	private synchronized void initializeData(Map<String, Object> userData) {
		if (!initialized && userData != null) {
			sessionData = new SessionData();
			sessionData.setUserId((long) userData.get("userId"));
			sessionData.setOrganizationId((long) userData.get("organizationId"));
			sessionData.setEncodedUserId(idEncoder.encodeId((long) userData.get("userId")));
			sessionData.setName((String) userData.get("name"));
			sessionData.setLastName((String) userData.get("lastname"));
			sessionData.setFullName(setFullName(sessionData.getName(), sessionData.getLastName()));
			sessionData.setAvatar((String) userData.get("avatar"));
			sessionData.setPosition((String) userData.get("position"));
			sessionData.setIpAddress((String) userData.get("ipAddress"));
//			sessionData.setPagesTutorialPlayed((Set<String>) userData.get("pagesTutorialPlayed"));
			sessionData.setSelectedStatusWallFilter((Filter) userData.get("statusWallFilter"));
			sessionData.setUserSettings((UserSettings) userData.get("userSettings"));
			sessionData.setEmail((String) userData.get("email"));
			sessionData.setNotificationsSettings((List<NotificationSettingsData>) userData.get("notificationsSettings"));
			sessionData.setPassword((String) userData.get("password"));
			sessionData.setSessionId((String) userData.get("sessionId"));
			initialized = true;
		}
	}
	
	public void reinitializeSessionData(UserData user, long organizationId) {
		if (user != null) {
			sessionData.setUserId(user.getId());
			sessionData.setOrganizationId(organizationId);
			sessionData.setEncodedUserId(idEncoder.encodeId(user.getId()));
			sessionData.setName(user.getName());
			sessionData.setLastName(user.getLastName());
			sessionData.setFullName(setFullName(sessionData.getName(), sessionData.getLastName()));
			sessionData.setAvatar(AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120));
			sessionData.setPosition(user.getPosition());
			sessionData.setEmail(user.getEmail());
			sessionData.setFullName(setFullName(user.getName(), user.getLastName()));
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
		setAvatar(AvatarUtils.getAvatarUrlInFormat(getSessionData().getAvatar(), ImageFormat.size120x120));
	}

	public boolean isLoggedIn() {
		return getSessionData() != null;
	}

	public void loadStatusWallFilter(FilterType chosenFilterType, long courseId) {
		Filter filter = sessionDataLoader.loadStatusWallFilter(getUserId(), chosenFilterType, courseId);
		setSelectedStatusWallFilter(filter);
	}

//	public void loadGoalWallFilter(long targetLearningGoal) {
//		LearningGoalFilter filter = new LearningGoalFilter(targetLearningGoal);
//		Long goalId = learningGoalManager.getGoalIdForTargetGoal(targetLearningGoal);
//		filter.setGoalId(goalId);
//		Set<Long> collaborators = learningGoalManager.getCollaboratorsIdsByTargetGoalId(targetLearningGoal);
//		filter.setCollaborators(collaborators);
//		Set<Long> tComps = learningGoalManager.getTargetCompetencesForTargetLearningGoal(targetLearningGoal);
//		filter.setTargetCompetences(tComps);
//		Set<Long> tActivities = learningGoalManager.getTargetActivitiesForTargetLearningGoal(targetLearningGoal);
//		filter.setTargetActivities(tActivities);
//
//		setSelectedLearningGoalFilter(filter);
//	}

	public void refreshUserSettings() {
		try {
			setUserSettings(interfaceSettingsManager.getOrCreateUserSettings(getUserId()));
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}

	public boolean loginUserOpenId(String email, String context) {
		try {
			boolean loggedIn = authenticationService.loginUserOpenID(email);
			if (loggedIn) {
				logger.info("LOGGED IN:" + email);
				init(email);
				logger.info("Initialized");

				logger.info("LOGING EVENT");
				// this.checkIpAddress();
				String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
				PageContextData lcd = new PageContextData(page, context, null);
				loggingService.logEvent(EventType.LOGIN, getUserContext(lcd), getIpAddress());
				return true;
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return false;
	}

	public void loginOpenId(String email) {
		boolean loggedIn = loginUserOpenId(email, null);

		if (loggedIn) {
			logger.info("REDIRECTING TO HOME PAGE FOR THE USER");

			PageUtil.redirect(new HomePageResolver().getHomeUrl(getOrganizationId()));
		} else {
			PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
			PageUtil.redirect("/login?faces-redirect=true");
		}
	}
	
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		if (initialized) {
			final String ipAddress = this.getIpAddress();
			//loggingService.logEvent(EventType.LOGOUT, user, ipAddress);
			//delete all temp files for this user
			try {
				FileUtils.deleteDirectory(new File(Settings.getInstance().config.fileManagement.uploadPath + 
						File.separator + getUserId()));
			} catch (IOException e) {
				logger.error(e);
			}
			userManager.fullCacheClear();
			if (getUserId() > 0) {
				Map<String, String> parameters = new HashMap<>();
				parameters.put("ip", ipAddress);
				eventFactory.generateEvent(EventType.SESSIONENDED, UserContextData.of(
						getUserId(), getOrganizationId(), event.getSession().getId(), null),
						null, null, null, parameters);

				userManager.fullCacheClear();
				logger.debug("UserSession unbound:" + event.getName() + " session:" + event.getSession().getId()
						+ " for user:" + getUserId());
			}
		}
		// else{
		// logger.debug("UserSession unbound:" + event.getName() + " session:" +
		// event.getSession().getId());
		// }
		applicationBean.unregisterSession(event.getSession());
		applicationBean.unregisterUser(getUserId());
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

			taskExecutor.execute(() -> {
				Session session = (Session) userManager.getPersistence().openSession();

				try {
					setUserSettings(interfaceSettingsManager.tutorialsPlayed(getUserId(), page, session));
					session.flush();
				} catch (Exception e) {
					logger.error("Exception in handling message", e);
				} finally {
					HibernateUtil.close(session);
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
	
	private Authentication getAuthenticationObject() {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context == null) {
			return null;
		}

		return context.getAuthentication();
	}
	
	public void userLogout(){
		try {
			final String ipAddress = this.getIpAddress();
			loggingService.logEvent(EventType.LOGOUT, getUserContext(), ipAddress);
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
					.getExternalContext().getRequest();
			String contextP = req.getContextPath() == "/" ? "" : req.getContextPath();
			Authentication auth = getAuthenticationObject();
			if(auth != null) {
				if(auth.getCredentials() instanceof SAMLCredential) {
					PageUtil.redirect("/saml/logout");
				} else {
					PageUtil.redirect("/logout");
				}
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	public void forceUserLogout(){
		try {
			final String ipAddress = this.getIpAddress();
			loggingService.logEvent(EventType.LOGOUT, getUserContext(), ipAddress);
			FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	public void prepareLoginAsUser(UserData user) {
		loginAsUser = user;
	}

	public void loginAs() {
		try {
			forceUserLogout();
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			authenticationService.loginAs((HttpServletRequest)context.getRequest(),
					(HttpServletResponse) context.getResponse(), loginAsUser.getEmail());
		} catch(AuthenticationException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while trying to login as " + loginAsUser.getFullName());
		}
	}

	public UserContextData getUserContext() {
		return getUserContext(PageUtil.extractLearningContextData());
	}

	public UserContextData getUserContext(PageContextData context) {
		return UserContextData.of(getUserId(), getOrganizationId(), getSessionId(),
				context);
	}

	public UserContextData getUserContext(long organizationId) {
		return UserContextData.of(getUserId(), organizationId, getSessionId(), PageUtil.extractLearningContextData());
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
		SessionData sData=getSessionData();
		sData.setEmail(email);
	}

	public String getPassword() {
		return getSessionData() == null ? null : getSessionData().getPassword();
	}

	public void setPassword(String password) {
		getSessionData().setPassword(password);
	}
	
	public long getUserId() {
		return getSessionData() == null ? 0 : getSessionData().getUserId();
	}

	public String getName() {
		return getSessionData() == null ? null : getSessionData().getName();
	}

	public String getLastName() {
		return getSessionData() == null ? null : getSessionData().getLastName();
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

	public String getAvatar() {
		return getSessionData() == null ? null : getSessionData().getAvatar();
	}

	public void setAvatar(String avatar) {
		getSessionData().setAvatar(avatar);
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

	public List<NotificationSettingsData> getNotificationsSettings() {
		return getSessionData() == null ? null : getSessionData().getNotificationsSettings();
	}

	public void setNotificationsSettings(List<NotificationSettingsData> notificationsSettings) {
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

	public UserData getLoginAsUser() {
		return loginAsUser;
	}

	public long getOrganizationId() {
		/*
		if organization id is 0 (which can happen for super admins who are not associated with organization)
		we try to retrieve organization id from url query param
		 */
		return getSessionData() == null
				? 0
				: getSessionData().getOrganizationId() > 0
					? getSessionData().getOrganizationId()
					: getOrganizationIdFromUrlQueryParam();
	}

	public long getOrganizationId(HttpServletRequest req) {
		/*
		try to get organization id the standard way and if 0, try to retrieve it from reqest parameters
		 */
		return getSessionData() == null
				? 0
				: getSessionData().getOrganizationId() > 0
					? getSessionData().getOrganizationId()
					: getOrganizationIdFromRequestParameters(req);
	}

	/**
	 * Query param name should be 'orgId' in order to be extracted
	 *
	 * @return
	 */
	private long getOrganizationIdFromUrlQueryParam() {
		if (FacesContext.getCurrentInstance() != null) {
			String orgId = PageUtil.getGetParameter("orgId");
			return idEncoder.decodeId(orgId);
		} else {
			return 0L;
		}
	}

	/**
	 * Query param name should be 'orgId' in order to be extracted
	 *
	 * @return
	 */
	private long getOrganizationIdFromRequestParameters(HttpServletRequest req) {
		if (req != null) {
			String orgId = req.getParameter("orgId");
			return idEncoder.decodeId(orgId);
		} else {
			return 0L;
		}
	}

	public String getSessionId() {
		return getSessionData() == null ? null : getSessionData().getSessionId();
	}
}