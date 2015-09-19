package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
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
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.web.util.AvatarUtils;
import org.prosolo.core.hibernate.HibernateUtil;
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
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name="loggeduser")
@Component("loggeduser")
@Scope("session")
public class LoggedUserBean implements Serializable, HttpSessionBindingListener  {
	
	private static final long serialVersionUID = 1404040093737456717L;

	private static Logger logger = Logger.getLogger(LoggedUserBean.class);
	
	@Autowired private UserManager userManager;
	@Autowired private AuthenticationService authenticationService;
	@Autowired private InterfaceSettingsManager interfaceSettingsManager;
	@Autowired private NotificationsSettingsManager notificationsSettingsManager;
	@Autowired private ApplicationBean applicationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LoggingService loggingService;
	@Autowired private AccessResolver accessResolver;
	
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private TagManager tagManager;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private CourseManager courseManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private SessionCountBean sessionCounter;

	private User user;
	private String email;
	private String password;
	private long loginTime;
	private String bigAvatar;
	private boolean doNotShowTutorial;
	private Set<String> pagesTutorialPlayed = new HashSet<String>();
	private List<Role> roles;
	private String ipAddress;
	
	private Filter selectedStatusWallFilter;
	private LearningGoalFilter selectedLearningGoalFilter;
	
	private UserSettings userSettings;
	private UserNotificationsSettings notificationsSettings;
	
	public void init(User user) {
		logger.info("init");
		this.user = user;
		this.loginTime = new Date().getTime();
		initializeRoles(user);
		initializeAvatar();
		registerNewUserSession();
		refreshUserSettings();
		refreshNotificationsSettings();
		
		FilterType chosenFilterType = this.userSettings.getActivityWallSettings().getChosenFilter();
		
		loadStatusWallFilter(chosenFilterType, userSettings.getActivityWallSettings().getCourseId());
		
		this.pagesTutorialPlayed = userSettings.getPagesTutorialPlayed();
		logger.info("init finished");
	}
	
 	public String getIpAddress() {
		return ipAddress;
	}
 	
	public void initializeRoles(User user) {
		if(user!=null)
		this.roles = new LinkedList<Role>(user.getRoles());

	}

	public void initializeAvatar() {
		this.bigAvatar = AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120);
	}
	
	private void registerNewUserSession(){
	 	HttpSession session=(HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	 	applicationBean.registerNewUserSession(this.user, session);
		sessionCounter.addSession(session.getId());
	}
	
	public boolean isLoggedIn(){
		return user != null;
	}
	
	public User refreshUser() {
 		user = userManager.merge(user);
 		refreshUserSettings();
 		return user;
	}
	
	public void loadStatusWallFilter(FilterType chosenFilterType, long courseId) {
		if (chosenFilterType.equals(FilterType.MY_NETWORK)) {
			this.selectedStatusWallFilter = new MyNetworkFilter();
			Set<Long> myNetworkUsers = activityWallManager.getUsersInMyNetwork(user.getId());
			((MyNetworkFilter) this.selectedStatusWallFilter).setUserIds(myNetworkUsers);
		} else if (chosenFilterType.equals(FilterType.MY_ACTIVITIES)) {
			this.selectedStatusWallFilter = new MyActivitiesFilter();
		} else if (chosenFilterType.equals(FilterType.ALL)) {
			this.selectedStatusWallFilter = new AllFilter();
		} else if (chosenFilterType.equals(FilterType.TWITTER)) {
			TwitterFilter twitterFilter = new TwitterFilter();
			twitterFilter.setHashtags(new TreeSet<Tag>(tagManager.getSubscribedHashtags(user)));
			this.selectedStatusWallFilter = twitterFilter;
		} else if (chosenFilterType.equals(FilterType.ALL_PROSOLO)) {
			this.selectedStatusWallFilter = new AllProsoloFilter();
		} else if (chosenFilterType.equals(FilterType.COURSE)) {
			CourseFilter courseFilter = new CourseFilter();
			try {
				Course course = userManager.loadResource(Course.class, courseId);
				courseFilter.setCourseId(courseId);
				Map<String, Set<Long>> goalTargetGoals = courseManager.getTargetLearningGoalIdsForCourse(course);
				//Long targetLearningGoalId=courseManager.getTargetLearningGoalIdForCourse(user, course);
				courseFilter.setTargetLearningGoals(goalTargetGoals.get("targetGoals"));
				//Long goalId=learningGoalManager.getGoalIdForTargetGoal(targetLearningGoalId);
				courseFilter.setLearningGoals(goalTargetGoals.get("goals"));
				Set<Long> tComps = courseManager.getTargetCompetencesForCourse(course);
				courseFilter.setTargetCompetences(tComps);
				Set<Long> tActivities = courseManager.getTargetActivitiesForCourse(course);
				courseFilter.setTargetActivities(tActivities);
				/*Set<Long> tComps=competenceManager.getTargetCompetencesIds(user.getId(), goalId);
				courseFilter.setTargetCompetences(tComps);
				Set<Long> targetActivities=new TreeSet<Long>();
				for(Long tc:tComps){
					Set<Long> ta=competenceManager.getTargetActivities(tc);
					targetActivities.addAll(ta);
				}
				courseFilter.setTargetActivities(targetActivities);*/
				
				Set<Tag> hashtags = course.getHashtags();
				for (Tag tag : hashtags) {
					courseFilter.addHashtag(tag.getTitle());
				}
				//courseFilter.setHashtags(course.getHashtags());
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}
			this.selectedStatusWallFilter = courseFilter;
		}
	}
	
	public void loadGoalWallFilter(long targetLearningGoal){
		LearningGoalFilter filter = new LearningGoalFilter(targetLearningGoal);
		Long goalId = learningGoalManager.getGoalIdForTargetGoal(targetLearningGoal);
		filter.setGoalId(goalId);
		Set<Long> collaborators = learningGoalManager.getCollaboratorsIdsByTargetGoalId(targetLearningGoal);
		filter.setCollaborators(collaborators);
		Set<Long> tComps = learningGoalManager.getTargetCompetencesForTargetLearningGoal(targetLearningGoal);
		filter.setTargetCompetences(tComps);
		Set<Long> tActivities = learningGoalManager.getTargetActivitiesForTargetLearningGoal(targetLearningGoal);
		filter.setTargetActivities(tActivities);
		
		this.selectedLearningGoalFilter = filter;
	}
	
	public void refreshUserSettings() {
		this.userSettings = interfaceSettingsManager.getOrCreateUserSettings(user); 
	}
	
	public void refreshNotificationsSettings() {
		this.notificationsSettings = notificationsSettingsManager.getOrCreateNotificationsSettings(user); 
	}
	
	public void loginOpenId(String email){
		boolean loggedIn;
		try {
			loggedIn = authenticationService.loginOpenId(email);
			
			if (loggedIn) {
				logger.info("LOGGED IN:" + email);
				this.email = email;
				// ((HttpSession)
				// FacesContext.getCurrentInstance().getExternalContext().getSession(false)).setAttribute("user",
				// value)
				init(userManager.getUser(email));
				logger.info("Initialized");
				ipAddress = accessResolver.findRemoteIPAddress();
				logger.info("LOGING EVENT");
				// this.checkIpAddress();
				loggingService.logEvent(EventType.LOGIN, this.user, this.getIpAddress());
				// return "index?faces-redirect=true";
				logger.info("REDIRECTING TO INDEX");
				FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
				return;
			}
		} catch (org.prosolo.services.authentication.exceptions.AuthenticationException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/login.xhtml?faces-redirect=true");
			return;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void login() {
		logger.debug("User \""+email+"\" is authenticating");		
		
		try {
			boolean loggedIn = authenticationService.login(email, password);				
			
			if (loggedIn) {
				logger.debug("User \"" + email + "\" authenticated successfully");
				init(userManager.getUser(email));
				logger.debug("User \"" + email + "\" initialized");
				ipAddress = accessResolver.findRemoteIPAddress();
				logger.debug("User \"" + email + "\" IP address:" + ipAddress);
				eventFactory.generateEvent(EventType.LOGIN, user);
				logger.debug("User \"" + email + "\" redirecting user to index page");
				FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
				return;
			}
		
			PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
			FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
		} catch (org.prosolo.services.authentication.exceptions.AuthenticationException e) {
			logger.debug("Authentication exception", e);
			
			PageUtil.fireErrorMessage("loginMessage", "Email or password incorrect.", null);
			return;
		} catch (Exception ex) {
			logger.error("Logging exception", ex);
		}
	}
	
	public void checkIfLoggedIn() {
		if (isLoggedIn()) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logout() {
		final String ipAddress = this.getIpAddress();
		
		loggingService.logEvent(EventType.LOGOUT, user, ipAddress);
		    	
		userManager.fullCacheClear();
		
		HttpSession untegisteredSession = applicationBean.unregisterUser(user);
		
		if (untegisteredSession != null)
			applicationBean.unregisterSession(untegisteredSession);
		
		authenticationService.logout();
		user = null;
		
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml?faces-redirect=true");
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		if (user != null) {
			loggingService.logEvent(EventType.SESSIONENDED, user, this.getIpAddress());
			userManager.fullCacheClear();
			logger.debug("UserSession unbound:" + event.getName() + " session:" + event.getSession().getId()+" for user:"+user.getId());
		}
		//else{
			//logger.debug("UserSession unbound:" + event.getName() + " session:" + event.getSession().getId());
		//}
		applicationBean.unregisterSession(event.getSession());
		applicationBean.unregisterUser(user);
	}
	
	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		//logger.debug("UserSession bound");
	}
	
	public boolean toPlayTutorial(String page) {
		if (page != null) {
			return !pagesTutorialPlayed.contains(page);
		}
		return false;
	}
	
	public void tutorialPlayed(boolean playButtonPressed, final String page) {
		if (playButtonPressed || doNotShowTutorial) {
			pagesTutorialPlayed.add(page);
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					Session session = (Session) userManager.getPersistence().openSession();
					
					try {
						userSettings = interfaceSettingsManager.tutorialsPlayed(user.getId(), page, session);
						
						session.flush();
					} catch (Exception e) {
						logger.error("Exception in handling message", e);
					}
					finally {
						HibernateUtil.close(session);
					}
				}
			});
		}
		this.doNotShowTutorial = false;
	}
	
	/* 
	 * GETTERS / SETTERS 
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return getUser().getName();
	}
	
	public String getLastName() {
		return getUser().getLastname();
	}
	
	public UserSettings getInterfaceSettings() {
		return userSettings;
	}
	
	public Locale getLocale() {
		if (isLoggedIn() && userSettings != null && userSettings.getLocaleSettings() != null) {
			return userSettings.getLocaleSettings().createLocale();
		} else {
			return new Locale("en", "US");
		}
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public String getBigAvatar() {
		return bigAvatar;
	}

	public void setBigAvatar(String bigAvatar) {
		this.bigAvatar = bigAvatar;
	}
    
	public boolean isManager() {
		return hasRole("MANAGER");
	}

	public boolean hasRole(String roleName) {
		if (roles != null) {
			for (Role role : roles) {
				if (roleName.equalsIgnoreCase(role.getTitle())) {
					return true;
				}
			}
		}
		return false;
	}
 
	public boolean isDoNotShowTutorial() {
		return doNotShowTutorial;
	}

	public void setDoNotShowTutorial(boolean doNotShowTutorial) {
		this.doNotShowTutorial = doNotShowTutorial;
	}
	public Filter getSelectedStatusWallFilter() {
		return selectedStatusWallFilter;
	}

	public void setSelectedStatusWallFilter(Filter selectedStatusWallFilter) {
		this.selectedStatusWallFilter = selectedStatusWallFilter;
	}
	
	public LearningGoalFilter getSelectedLearningGoalFilter() {
		return selectedLearningGoalFilter;
	}

	public void setSelectedLearningGoalFilter(LearningGoalFilter selectedLearningGoalFilter) {
		this.selectedLearningGoalFilter = selectedLearningGoalFilter;
	}

	public UserNotificationsSettings getNotificationsSettings() {
		return notificationsSettings;
	}

	public void setNotificationsSettings(UserNotificationsSettings notificationsSettings) {
		this.notificationsSettings = notificationsSettings;
	}
	
}