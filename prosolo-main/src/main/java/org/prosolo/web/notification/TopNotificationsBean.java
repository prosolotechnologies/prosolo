package org.prosolo.web.notification;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.NodeRequest;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.activitywall.old.SocialActivity;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CoursePortfolio;
import org.prosolo.common.domainmodel.course.Status;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.evaluation.EvaluationSubmission;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.CommentNotification;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.user.notifications.NotificationAction;
import org.prosolo.common.domainmodel.user.notifications.PostNotification;
import org.prosolo.common.domainmodel.user.notifications.SActivityNotification;
import org.prosolo.common.exceptions.KeyNotFoundInBundleException;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interfaceSettings.InterfaceCacheObserver;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.exceptions.EvaluationNotSupportedException;
import org.prosolo.services.nodes.exceptions.InvalidParameterException;
import org.prosolo.services.notifications.NotificationManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.notification.data.GoalStatus;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.prosolo.web.notification.util.NotificationUtil;
import org.prosolo.web.util.PageUtil;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Deprecated
@ManagedBean(name = "topNotificationsBean")
@Component("topNotificationsBean")
@Scope("session")
public class TopNotificationsBean {
	
	private static Logger logger = Logger.getLogger(TopNotificationsBean.class);
	
	@Autowired private NotificationManager notificationsManager;
	@Autowired private EventFactory eventFactory;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private CourseManager courseManager;
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LearnBean goalsBean;
	@Autowired private ApplicationBean applicationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private ActivityWallManager activityWallManager;
	
	private LinkedList<NotificationData> notifications;
	private int unreadNotificationsNo;
	
	private Date lastViewed;
	
	private int notificationsLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private int refreshRate = Settings.getInstance().config.application.defaultRefreshRate;
	
	@PostConstruct
	public void initNotificationsNo() { 
		logger.debug("Initializing unread notifications number.");
		
		User user = loggedUser.getUser();
		
		if (user != null)
			this.unreadNotificationsNo = notificationsManager.getNumberOfUnreadNotifications(user);
	}
	
	public void initNotifications() {
		fetchNotifications((Session) goalManager.getPersistence().currentManager());
		
		for (int i = 0; i < notifications.size(); i++) {
			NotificationData notificationData = notifications.get(i);
			
			if (!notificationData.isRead()) {
				if (lastViewed != null && notificationData.getCreated().before(lastViewed)) {
					notificationData.setRead(true);
				}
			}
		}
		
		final User user = loggedUser.getUser();
		if (unreadNotificationsNo > 0) {
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	Session session = (Session) notificationsManager.getPersistence().openSession();
					try {
						notificationsManager.markAsReadAllUnreadNotifications(user, session);
					} finally {
						HibernateUtil.close(session);
					}
	            }
	        });
		}
		
		lastViewed = new Date();
		unreadNotificationsNo = 0;
	}

	private void fetchNotifications(Session session) {
		if (notifications == null) {
			logger.debug("Initializing notifications.");
			
			try {
				List<Notification> latestNotifications = notificationsManager.getNotifications(
						loggedUser.getUser(), 
						0, 
						notificationsLimit);
				
				this.notifications = NotificationDataConverter.convertNotifications(
						loggedUser.getUser(),
						latestNotifications,
						session,
						loggedUser.getLocale());
			} catch (NotificationNotSupported e) {
				logger.error(e);
			}
		}
	}

	public synchronized void addNotification(NotificationData notificationData, Session session){
		if (notifications == null) {
			fetchNotifications(session);
		} else {
			notifications.addFirst(notificationData);
		}

		unreadNotificationsNo++;
		
		if (notifications.size() > notificationsLimit) {
			Iterator<NotificationData> iterator = notifications.iterator();
			int index = 1;
			
			while (iterator.hasNext()) {
				iterator.next();
				
				if (index > notificationsLimit) {
					iterator.remove();
				}
				index++;
			}
		}
	}
	
	public void notificationDismissed(NotificationData notificationData) {
		try {
			Notification notification = goalManager.loadResource(Notification.class, notificationData.getId());
			
			Request request = (Request) notification.getObject();
			request.setStatus(RequestStatus.IGNORED);
			notification.setChosenAction(NotificationAction.IGNORE);
			goalManager.saveEntity(request);
			goalManager.saveEntity(notification);
			PageUtil.fireInfoMessage(null, "You have dismissed request to join learning goal", request.getResource().getTitle());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
	}
	
	public void invokeAction(String actionName, NotificationData notificationData) {
		NotificationAction action = NotificationAction.valueOf(actionName);
		try {
			Notification notification = goalManager.loadResource(Notification.class, notificationData.getId());
		
			switch (action) {
				case ACCEPT:
					acceptNotification(notificationData);
					break;
				case IGNORE:
					ignoreNotification(notificationData);
					break;
				case VIEW:
					viewNotification(notification);
					break;
				case DENY:
					denyNotification(notificationData);
					break;
				default:
					break;
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (EventException e) {
			logger.error(e.getMessage());
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
		
		logNotificationsAction(actionName, notificationData);
	}
	

	/**
	 * @param notificationData
	 * @throws IOException
	 * @throws EventException
	 * @throws ResourceCouldNotBeLoadedException
	 */
	public void acceptNotification(NotificationData notificationData) throws IOException, EventException, ResourceCouldNotBeLoadedException {
		Notification notification = notificationsManager.markNotificationStatus(notificationData.getId(), NotificationAction.ACCEPT);
		
		notificationData.setChosenAction(NotificationUtil.getNotificationChosenActionName(NotificationAction.ACCEPT, loggedUser.getLocale()));
		notificationData.setUpdated(notification.getUpdated());
		
		Request request = (Request) notification.getObject();
		EventType requestType = request.getRequestType();
		
		switch (requestType) {
			case JOIN_GOAL_INVITATION:
				acceptJoinGoalInvitation(notificationData, request);
				break;
			case JOIN_GOAL_REQUEST:
				acceptJoinGoalRequest(request);
				break;
			case EVALUATION_REQUEST:
				acceptEvaluationRequest(notificationData, notification, request);
				break;
			default:
				break;
		}
	}

	private void acceptJoinGoalInvitation(NotificationData notificationData, Request request) throws EventException, IOException {
		try {
			final TargetLearningGoal targetGoalInvited = goalManager.merge((TargetLearningGoal) request.getResource());
			TargetLearningGoal newTargetGoal = null;
			
			GoalStatus goalStatus = notificationData.getGoalStatus();
			
			if (goalStatus.equals(GoalStatus.HAS_ACTIVE_COURSE_BASED_GOAL) || goalStatus.equals(GoalStatus.HAS_INACTIVE_COURSE_BASED_GOAL)) {
				newTargetGoal = joinUserToCourseBasedGoalWithLearningExperience(targetGoalInvited, loggedUser.getUser());
			} else if (goalStatus.equals(GoalStatus.NO_GOAL_COURSE_CONNECTED)) {
				newTargetGoal = joinUserToCourseBasedGoalForFirstTime(targetGoalInvited, loggedUser.getUser());
			} else if (goalStatus.equals(GoalStatus.HAS_GOAL_REGULAR)) {
				newTargetGoal = joinUserToRegularAlreadyHadGoal(targetGoalInvited, loggedUser.getUser());
			} else if (goalStatus.equals(GoalStatus.NO_GOAL_REGULAR)) {
				newTargetGoal = joinUserToRegularGoal(targetGoalInvited, loggedUser.getUser());
			}
			
			((NodeRequest) request).setResolutionResource(newTargetGoal);
			request = goalManager.saveEntity(request);
			goalManager.flush();

			eventFactory.generateEvent(EventType.JOIN_GOAL_INVITATION_ACCEPTED, loggedUser.getUser(), request);

			
			// add new goal to user's cache
			HttpSession userSession = applicationBean.getUserSession(loggedUser.getUser().getId());
			LearnBean learningGoalsBean = (LearnBean) userSession.getAttribute("learninggoals");
			
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().addGoal(
						loggedUser.getUser(), 
						newTargetGoal, 
						(Session) goalManager.getPersistence().currentManager());
			}
			
			
			// reset collaborators' caches
			InterfaceCacheObserver cacheUpdater = ServiceLocator.getInstance().getService(InterfaceCacheObserver.class);
			
			if (cacheUpdater != null) {
				cacheUpdater.asyncResetGoalCollaborators(newTargetGoal.getLearningGoal().getId(), loggedUser.getUser());
			}
			
			PageUtil.fireSuccessfulInfoMessage("notificationsGrowl", "You accepted request to join learning goal '"+ request.getResource().getTitle()+"'.");
			
			String redirectUrl = String.format("learn/%s", newTargetGoal.getId());
			
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			ec.getFlash().setKeepMessages(true);
			ec.redirect(ec.getRequestContextPath() + "/" + redirectUrl);
		}  catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
	}

	private void acceptJoinGoalRequest(Request request) throws EventException {
		try {
			TargetLearningGoal targetGoalToJoin = goalManager.merge((TargetLearningGoal) request.getResource());
			
			User requestMaker = request.getMaker();
			
			GoalStatus goalStatus = NotificationDataConverter.getGoalStatus(
					requestMaker, 
					(Session) goalManager.getPersistence().currentManager(), 
					targetGoalToJoin.getId());
			
			TargetLearningGoal newTargetGoal = null;
			
			if (goalStatus.equals(GoalStatus.HAS_ACTIVE_COURSE_BASED_GOAL) || goalStatus.equals(GoalStatus.HAS_INACTIVE_COURSE_BASED_GOAL)) {
				newTargetGoal = joinUserToCourseBasedGoalWithLearningExperience(targetGoalToJoin, requestMaker);
			} else if (goalStatus.equals(GoalStatus.NO_GOAL_COURSE_CONNECTED)) {
				newTargetGoal = joinUserToCourseBasedGoalForFirstTime(targetGoalToJoin, requestMaker);
			} else if (goalStatus.equals(GoalStatus.HAS_GOAL_REGULAR)) {
				newTargetGoal = joinUserToRegularAlreadyHadGoal(targetGoalToJoin, requestMaker);
			} else if (goalStatus.equals(GoalStatus.NO_GOAL_REGULAR)) {
				newTargetGoal = joinUserToRegularGoal(targetGoalToJoin, requestMaker);
			}
			
			// update request
			((NodeRequest) request).setResolutionResource(newTargetGoal);
			request = goalManager.saveEntity(request);
			goalManager.flush();

			eventFactory.generateEvent(EventType.JOIN_GOAL_REQUEST_APPROVED, loggedUser.getUser(), request);
			
			// add new goal to user's cache
			messageDistributer.distributeMessage(
					ServiceType.ADD_GOAL, 
					requestMaker.getId(), 
					newTargetGoal.getId(), 
					null, 
					null);
			
			// reset collaborators' caches
			InterfaceCacheObserver cacheUpdater = ServiceLocator.getInstance().getService(InterfaceCacheObserver.class);
			
			if (cacheUpdater != null) {
				cacheUpdater.asyncResetGoalCollaborators(newTargetGoal.getLearningGoal().getId(), requestMaker);
			}
			
			PageUtil.fireSuccessfulInfoMessage("notificationsGrowl", "You accepted "+requestMaker.getName()
					+" "+requestMaker.getLastname()+"'s request to join learning goal '"+ 
					request.getResource().getTitle()+"'.");
		}  catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e.getMessage());
		}
	}

	private void acceptEvaluationRequest(NotificationData notificationData, Notification notification, Request request) throws EventException,
			IOException {
		try {
			EvaluationSubmission evaluationSubmission = evaluationManager.createEvaluationSubmissionDraft(
					loggedUser.getUser(), 
					request);
			
			eventFactory.generateEvent(EventType.EVALUATION_ACCEPTED, loggedUser.getUser(), request);
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			
			try {
				PageUtil.fireSuccessfulInfoMessage("notificationsGrowl", 
						ResourceBundleUtil.getMessage(
								"notification.growl.acceptedToEvaluate", 
								loggedUser.getLocale(),
								notificationData.getResource().getShortType(),
								request.getResource().getTitle()));
				
				externalContext.getFlash().setKeepMessages(true);
			} catch (KeyNotFoundInBundleException e) {
				logger.error(e);
			}
			
			externalContext.redirect(externalContext.getRequestContextPath() + "/evaluation.xhtml?id="+evaluationSubmission.getId());
		} catch (InvalidParameterException e1) {
			logger.error(e1);
		} catch (EvaluationNotSupportedException e1) {
			logger.error(e1);
		} catch (ResourceCouldNotBeLoadedException e1) {
			logger.error(e1);
		}
	}
	
	private TargetLearningGoal joinUserToCourseBasedGoalWithLearningExperience(final TargetLearningGoal targetGoalInvited, User user) throws EventException,
			ResourceCouldNotBeLoadedException {
		TargetLearningGoal newTargetGoal = goalManager.createNewTargetLearningGoal(user, targetGoalInvited.getLearningGoal());
		
		TargetLearningGoal existingGoal = courseManager.getTargetLearningGoalForCourse(user, targetGoalInvited.getCourseEnrollment().getCourse());
		
		newTargetGoal = goalManager.cloneTargetCompetencesFromOneGoalToAnother(existingGoal, newTargetGoal);
		goalManager.markAsDeleted(existingGoal);
		
		
		CourseEnrollment enrollment = existingGoal.getCourseEnrollment();
		enrollment.setTargetGoal(newTargetGoal);
		enrollment.setStatus(Status.ACTIVE);
		enrollment = goalManager.saveEntity(enrollment);
		
		newTargetGoal.setCourseEnrollment(enrollment);
		newTargetGoal = goalManager.saveEntity(newTargetGoal);
		
		CoursePortfolio coursePortfolio = courseManager.getCoursePortfolio(user);
		
		if (coursePortfolio != null)
			courseManager.addEnrollment(coursePortfolio, enrollment);
		

		// update goals cache
		messageDistributer.distributeMessage(
			ServiceType.REMOVE_TARGET_GOAL, 
			user.getId(), 
			existingGoal.getId(), 
			null, 
			null);
		
		// update Course Portfolio
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("enrollmentId", String.valueOf(enrollment.getId()));

		messageDistributer.distributeMessage(
			ServiceType.ADD_ACTIVE_COURSE,
			user.getId(),
			enrollment.getCourse().getId(), 
			null, 
			parameters);
		
		return newTargetGoal;
	}

	private TargetLearningGoal joinUserToCourseBasedGoalForFirstTime(final TargetLearningGoal targetGoalInvited, User user) throws EventException, ResourceCouldNotBeLoadedException {
		TargetLearningGoal newTargetGoal = goalManager.createNewCourseBasedLearningGoal(user, targetGoalInvited.getCourseEnrollment().getCourse().getId(), targetGoalInvited.getLearningGoal(), "notifications.accept");
		
		CourseEnrollment enrollment = courseManager.enrollInCourse(user, targetGoalInvited.getCourseEnrollment().getCourse().getId(), newTargetGoal, "notifications.accept", null, null, null);
		
		newTargetGoal.setCourseEnrollment(enrollment);
		newTargetGoal = goalManager.saveEntity(newTargetGoal);
		
		CoursePortfolio coursePortfolio = courseManager.getCoursePortfolio(user);
		
		if (coursePortfolio != null)
			courseManager.addEnrollment(coursePortfolio, enrollment);
		
		
		// update Course Portfolio
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("enrollmentId", String.valueOf(enrollment.getId()));

		messageDistributer.distributeMessage(
			ServiceType.ADD_ACTIVE_COURSE,
			user.getId(),
			enrollment.getCourse().getId(), 
			null, 
			parameters);
		
		return newTargetGoal;
	}
	
	private TargetLearningGoal joinUserToRegularAlreadyHadGoal(final TargetLearningGoal targetGoalInvited, User user) throws EventException,
		ResourceCouldNotBeLoadedException {
		TargetLearningGoal existingGoal = goalManager.getTargetGoal(targetGoalInvited.getLearningGoal().getId(), user.getId());
		
		TargetLearningGoal newTargetGoal = goalManager.createNewTargetLearningGoal(user, targetGoalInvited.getLearningGoal());
		newTargetGoal = goalManager.cloneTargetCompetencesFromOneGoalToAnother(existingGoal, newTargetGoal);
		goalManager.markAsDeleted(existingGoal);
		
		// update goals cache
		messageDistributer.distributeMessage(
			ServiceType.REMOVE_TARGET_GOAL, 
			user.getId(), 
			existingGoal.getId(), 
			null, 
			null);
		
		return newTargetGoal;
	}
	
	private TargetLearningGoal joinUserToRegularGoal(final TargetLearningGoal targetGoalInvited, User user) throws EventException,
		ResourceCouldNotBeLoadedException {
	
		TargetLearningGoal newTargetGoal = goalManager.createNewTargetLearningGoal(user, targetGoalInvited.getLearningGoal());
	
		return newTargetGoal;
	}

	public void ignoreNotification(NotificationData notificationData) throws ResourceCouldNotBeLoadedException {
		Notification notification = notificationsManager.markNotificationStatus(notificationData.getId(), NotificationAction.IGNORE);
		
		notificationData.setChosenAction(NotificationUtil.getNotificationChosenActionName(NotificationAction.IGNORE, loggedUser.getLocale()));
		notificationData.setUpdated(notification.getUpdated());
		
		try {
			eventFactory.generateEvent(EventType.JOIN_GOAL_REQUEST_IGNORED, loggedUser.getUser(), (Request) notification.getObject());
		} catch (EventException e) {
			logger.error(e);
		}
		
		PageUtil.fireSuccessfulInfoMessage("notificationsGrowl", "You have ignored this notification.");
	}

	public void viewNotification(Notification notification) throws IOException {
		BaseEntity resource = notification.getObject();
		
		EventType type = notification.getType();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		
		if (type.equals(EventType.EVALUATION_GIVEN)) {
			EvaluationSubmission evaluationSubmition = (EvaluationSubmission) resource;
			evaluationSubmition = goalManager.merge(evaluationSubmition);
			BaseEntity evaluatedResource = evaluationSubmition.getRequest().getResource();
			
			Evaluation evaluation = null;
			
			if (evaluatedResource instanceof TargetCompetence) {
				Set<Evaluation> evaluations = evaluationSubmition.getEvaluations();
				
				if (evaluations != null && !evaluations.isEmpty())
					evaluation = evaluations.iterator().next();
				
				// this means we should redirect to the goals page. But first, we need
				// to get goal's id and set this target competence as selected
				
				TargetCompetence tComp = (TargetCompetence) evaluatedResource;
				TargetLearningGoal targetGoal = tComp.getParentGoal();
				
				externalContext.redirect(
						externalContext.getRequestContextPath() + 
						"/learn" +
						"/" + targetGoal.getId() +
						"/" + tComp.getId() +
						"#ev=" + evaluation.getId()
						);
				return;
			}
			
			// TODO
			if (evaluatedResource instanceof CompletedGoal || evaluatedResource instanceof TargetLearningGoal) {
				evaluation = evaluationSubmition.filterEvaluationForLearningGoal();
				
				if (evaluatedResource instanceof TargetLearningGoal) {
					TargetLearningGoal targetGoal = (TargetLearningGoal) evaluatedResource;
					
					GoalDataCache goalData = goalsBean.getData().getDataForTargetGoal(targetGoal.getId());
					
					if (goalData != null) {
						goalData.fetchEvaluations();
						
						externalContext.redirect(
								externalContext.getRequestContextPath() + 
								"/learn.xhtml?" +
								"id=" + targetGoal.getId() +
								"&showCompetences=false" +
								"&faces-redirect=true"+
								"#ev=" + evaluation.getId()
								);
						return;
					}
				}
			} else if (evaluatedResource instanceof AchievedCompetence) {
				Set<Evaluation> evaluations = evaluationSubmition.getEvaluations();
				
				if (evaluations != null && !evaluations.isEmpty()) {
					evaluation = evaluations.iterator().next();
				}
			} else if (evaluatedResource instanceof ExternalCredit) {
				Set<Evaluation> evaluations = evaluationSubmition.getEvaluations();
				
				if (evaluations != null && !evaluations.isEmpty()) {
					for (Evaluation ev : evaluations) {
						if (ev.getResource() instanceof ExternalCredit) {
							evaluation = ev;
							break;
						}
					}
				}
			}
			
			if (evaluation != null) {
				externalContext.redirect(externalContext.getRequestContextPath() + "/profile?ev="+evaluation.getId());
				return;
			}
		} else if (type.equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED)) {
			Request request = (Request) resource;
			
			externalContext.redirect(externalContext.getRequestContextPath() + "/learn/" + request.getResource().getId());
		} else if (type.equals(EventType.JOIN_GOAL_REQUEST_APPROVED)) {
			NodeRequest request = (NodeRequest) resource;
			
			externalContext.redirect(externalContext.getRequestContextPath() + "/learn/" + request.getResolutionResource().getId());
		} else if (type.equals(EventType.ACTIVITY_REPORT_AVAILABLE)){
			externalContext.redirect(externalContext.getRequestContextPath() + "/settings/reports");
		} else if (type.equals(EventType.Comment)) {
			notification = HibernateUtil.initializeAndUnproxy(notification);
			
			BaseEntity commentedResource = ((CommentNotification) notification).getComment().getObject();
			
			if (commentedResource instanceof SocialActivity) {
				externalContext.redirect(
						externalContext.getRequestContextPath() + 
						"/posts" +
						"/" + commentedResource.getId() + 
						"/" + resource.getId());
			}
		} else if (type.equals(EventType.Like) || type.equals(EventType.Dislike)) {
			notification = HibernateUtil.initializeAndUnproxy(notification);
			
			BaseEntity res = ((SActivityNotification) notification).getObject();
			
			if (res instanceof SocialActivity) {
				externalContext.redirect(
						externalContext.getRequestContextPath() + 
						"/posts" +
						"/"+res.getId());
			}
		} else if (type.equals(EventType.MENTIONED)) {
			notification = HibernateUtil.initializeAndUnproxy(notification);
			
			Post post = ((PostNotification) notification).getObject();
			
			SocialActivity socialActivity = activityWallManager.getSocialActivityOfPost(post);
			
			externalContext.redirect(
					externalContext.getRequestContextPath() + 
					"/posts" +
					"/" + socialActivity.getId());
		}
	}
	
	private void denyNotification(NotificationData notificationData) throws EventException, ResourceCouldNotBeLoadedException {
		Notification notification = notificationsManager.markNotificationStatus(notificationData.getId(), NotificationAction.DENY);
		
		notificationData.setChosenAction(NotificationUtil.getNotificationChosenActionName(NotificationAction.DENY, loggedUser.getLocale()));
		notificationData.setUpdated(notification.getUpdated());
		
		if (notification.getType().equals(EventType.JOIN_GOAL_REQUEST)) {
			Request request = (Request) notification.getObject();

			eventFactory.generateEvent(EventType.JOIN_GOAL_REQUEST_DENIED, loggedUser.getUser(), request);
		}
		PageUtil.fireSuccessfulInfoMessage("notificationsGrowl", "You have denied this request.");
	}

	public String getNotificationActionName(NotificationAction action) {
		return NotificationUtil.getNotificationActionName(action, loggedUser.getLocale());
	}
	
	public String getNotificationChosenActionName(NotificationAction action) {
		try {
			Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
			
			return ResourceBundleUtil.getMessage(
					"action.chosen.name."+action.toString(), 
					locale);
		} catch (KeyNotFoundInBundleException e) {
			logger.error("Coulc not find localized message for notification "+action+". " + e.getMessage());
			return "action";
		}
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	public List<NotificationData> getNotifications() {
		return notifications;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public int getUnreadNotificationsNo() {
		return unreadNotificationsNo;
	}
	
	public void setUnreadNotificationsNo(int unreadNotificationsNo) {
		this.unreadNotificationsNo = unreadNotificationsNo;
	}

	public void logNotificationServiceUse(){
		loggingNavigationBean.logServiceUse(
				ComponentName.NOTIFICATIONS,
				"action", "openNotifications",
				"unreadNotificationsNumber", String.valueOf(this.unreadNotificationsNo));
	}
	
	public void logNotificationsAction(String actionName,NotificationData notificationData) {
		long notificationId = 0;
		
		if (notificationData.getResource() != null) {
			notificationId = notificationData.getResource().getId();
		}
		
		loggingNavigationBean.logServiceUse(
				ComponentName.NOTIFICATIONS,
				"action",  actionName,
				"notificationId", String.valueOf(notificationId),
				"notificationType", notificationData.getType());
	}
}
