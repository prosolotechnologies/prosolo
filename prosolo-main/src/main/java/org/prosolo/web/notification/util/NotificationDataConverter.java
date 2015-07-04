			/**
 * 
 */
package org.prosolo.web.notification.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activities.requests.AchievedCompetenceRequest;
import org.prosolo.domainmodel.activities.requests.ExternalCreditRequest;
import org.prosolo.domainmodel.activities.requests.NodeRequest;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.comments.Comment;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.content.Post;
import org.prosolo.domainmodel.course.CourseEnrollment;
import org.prosolo.domainmodel.course.Status;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.CompletedGoal;
import org.prosolo.domainmodel.portfolio.CompletedResource;
import org.prosolo.domainmodel.portfolio.ExternalCredit;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.notifications.Notification;
import org.prosolo.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.util.date.DateUtil;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.notification.data.GoalStatus;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.exceptions.KeyNotFoundInBundleException;

/**
 * @author "Nikola Milikic"
 *
 */
public class NotificationDataConverter {
	
	private static Logger logger = Logger.getLogger(NotificationDataConverter.class);
	
	public static LinkedList<NotificationData> convertNotifications(User loggedUser, List<Notification> notifications, Session session, Locale locale) throws NotificationNotSupported {
		LinkedList<NotificationData> notData = new LinkedList<NotificationData>();
		
		if (notifications != null && !notifications.isEmpty()) {
			for (Notification n : notifications) {
				if (n != null)
					notData.add(convertNotification(loggedUser, n, session, locale));
			}
		}
		return notData;
	}

	/**
	 * @param loggedUser 
	 * @param notification
	 * @param locale 
	 * @return
	 */
	public static NotificationData convertNotification(User loggedUser, Notification notification, Session session, 
			Locale locale) throws NotificationNotSupported {

		EventType notificationType = notification.getType();
		BaseEntity object = notification.getObject();
		
		if (object != null) {
			object = (BaseEntity) session.merge(object);
		}
		
		NotificationData notificationData = new NotificationData();
		
		// if it is not responded notification and it is a request to join goal
		if (notification.getChosenAction() == null && notificationType.equals(EventType.JOIN_GOAL_INVITATION)) {
			
			if (object instanceof Request) {
				Request request = (Request) object;
				TargetLearningGoal targetGoal = (TargetLearningGoal) request.getResource();

				try {
					GoalStatus goalStatus = getGoalStatus(loggedUser, session, targetGoal.getId());
					notificationData.setGoalStatus(goalStatus);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// read
		notificationData.setRead(notification.isRead());
		
		
		// actor id
		User maker = notification.getActor();
		
		if (maker != null)
			notificationData.setActor(new UserData(maker));
		
		// type
		try {
			String type = ResourceBundleUtil.getMessage(
					"notification.type."+notificationType.toString(), 
					locale);
			notificationData.setType(type);
		} catch (KeyNotFoundInBundleException e) {
			logger.error(e);
		}
		
		// actions
		notificationData.setActions(notification.getActions());
		
		// chosen action
		notificationData.setChosenAction(NotificationUtil.getNotificationChosenActionName(notification.getChosenAction(), locale));
		// notification type
		notificationData.setNotificationType(notificationType);
		
		// notification
		notificationData.setId(notification.getId());
		
		// date
		notificationData.setDate(DateUtil.getPrettyDate(notification.getDateCreated()));
		
		notificationData.setCreated(notification.getDateCreated());
		
		// actionable
		notificationData.setActionable(notification.isActinable());

		// message
		notificationData.setMessage(notification.getMessage());
		
		if (object != null) {
			if (object instanceof Request) {
				Request request = (Request) object;
				// resource title
				BaseEntity resource = request.getResource();
				resource = (BaseEntity) session.merge(resource);
				resource = HibernateUtil.initializeAndUnproxy(resource);
				
				// resource
				if (resource instanceof CompletedResource) {
					if (resource instanceof CompletedGoal) {
						TargetLearningGoal targetGoal = ((CompletedGoal) resource).getTargetGoal();
						
						// initialize goal
						targetGoal = HibernateUtil.initializeAndUnproxy(targetGoal);
						
						notificationData.setResource(new NodeData(targetGoal));
					} else if (resource instanceof AchievedCompetence) {
						TargetCompetence targetCompetence = ((AchievedCompetence) resource).getTargetCompetence();
						
						// initialize target competence
						targetCompetence = HibernateUtil.initializeAndUnproxy(targetCompetence);
						
						notificationData.setResource(new NodeData(targetCompetence));
					} else if (resource instanceof ExternalCredit) {
						notificationData.setResource(new NodeData(resource));
					}
				} else {
					notificationData.setResource(new NodeData(resource));
				}
			} else if (object instanceof EvaluationSubmission) {
				EvaluationSubmission evSubmission = (EvaluationSubmission) object;
				Request request = evSubmission.getRequest();
				
				// is it Node request
				if (request instanceof NodeRequest) {
					
					// resource title
					Evaluation goalEvaluation = evSubmission.filterEvaluationForLearningGoal();
					
					if (goalEvaluation != null) {
						BaseEntity resource = goalEvaluation.getResource();
						resource = HibernateUtil.initializeAndUnproxy(resource);
						
						// resource
						if (resource instanceof CompletedGoal) {
							notificationData.setResource(new NodeData(((CompletedGoal) resource).getTargetGoal()));
						} else if (resource instanceof LearningGoal) {
							notificationData.setResource(new NodeData((LearningGoal) resource));
						} else if (resource instanceof TargetLearningGoal) {
							notificationData.setResource(new NodeData((TargetLearningGoal) resource));
						}
					} else {
						// it is Competence evaluation submission
						Set<Evaluation> evaluations = evSubmission.getEvaluations();
						
						if (evaluations != null && !evaluations.isEmpty()) {
							BaseEntity resource = evaluations.iterator().next().getResource();
							resource = HibernateUtil.initializeAndUnproxy(resource);
			
							if (resource instanceof AchievedCompetence) {
								notificationData.setResource(new NodeData(((AchievedCompetence) resource).getTargetCompetence()));
							} else {
								notificationData.setResource(new NodeData(resource));
							}
						}
					}
				} 
				// is it External Credit evaluation submitted
				else if (request instanceof ExternalCreditRequest) {
					Set<Evaluation> evaluations = evSubmission.getEvaluations();
					
					for (Evaluation evaluation : evaluations) {
						if (evaluation.getResource() instanceof ExternalCredit) {
							notificationData.setResource(new NodeData(evaluation.getResource()));
						}
					}
				}
				// is it Achieved Competence evaluation submitted
				else if (request instanceof AchievedCompetenceRequest) {
					Set<Evaluation> evaluations = evSubmission.getEvaluations();
					
					for (Evaluation evaluation : evaluations) {
						if (evaluation.getResource() instanceof AchievedCompetence) {
							notificationData.setResource(new NodeData(evaluation.getResource()));
						}
					}
				}
//			} else if (notificationType.equals(EventType.EVALUATION_EDITED)){
//				Evaluation evaluation = (Evaluation) object;
//				
//				BaseEntity resource = evaluation.getResource();
//			 	resource = HibernateUtil.initializeAndUnproxy(resource);
//				
//				// resource id
//				notificationData.setResource(new NodeData(resource));
			} else if (object instanceof Comment) {
				BaseEntity commentedResource = ((Comment) object).getObject();
				
				if (commentedResource instanceof Node) {
					notificationData.setResource(new NodeData(object));
				} else if (commentedResource instanceof SocialActivity) {
					notificationData.setResource(new NodeData());
					
					SocialActivity socialActivity = (SocialActivity) commentedResource;
					if (socialActivity.getObject() instanceof Post) {
						notificationData.getResource().setShortType(ResourceBundleUtil.getResourceType(socialActivity.getObject().getClass(), locale));
						notificationData.getResource().setClazz(Post.class);
					} else {
						notificationData.getResource().setShortType(ResourceBundleUtil.getResourceType(socialActivity.getClass(), locale));
						notificationData.getResource().setClazz(SocialActivity.class);
					}
				}
			} else if (object instanceof SocialActivity) {
				notificationData.setResource(new NodeData());
				
				BaseEntity resource = ((SocialActivity) object).getObject();
				if (notificationType.equals(EventType.Like) || notificationType.equals(EventType.Dislike)) {
					
					if (resource instanceof Post) {
						notificationData.getResource().setShortType(ResourceBundleUtil.getResourceType(resource.getClass(), locale));
						notificationData.getResource().setClazz(Post.class);
					} else {
						notificationData.getResource().setShortType(ResourceBundleUtil.getResourceType(object.getClass(), locale));
						notificationData.getResource().setClazz(SocialActivity.class);
					}
				}
			} else if (object instanceof Post) {
				notificationData.setResource(new NodeData());
				
				BaseEntity resource = ((Post) object);
				notificationData.getResource().setShortType(ResourceBundleUtil.getResourceType(resource.getClass(), locale));
				notificationData.getResource().setClazz(Post.class);
			} else {
				throw new NotificationNotSupported("Notification for a resource of a type "+
						object.getClass().getSimpleName()+
						" is not supported.");
			}
		}
		
		NodeData resource = notificationData.getResource();
		
		if (resource != null) {
			String resourceType = ResourceBundleUtil.getResourceType(resource.getClazz(), locale);
			resource.setShortType(resourceType);
		}
		
		return notificationData;
	}

	public static GoalStatus getGoalStatus(User loggedUser, Session session, long targetGoalId) throws ResourceCouldNotBeLoadedException {
		CourseManager courseManager = ServiceLocator.getInstance().getService(CourseManager.class);
		
		TargetLearningGoal targetGoalInvited = courseManager.loadResource(TargetLearningGoal.class, targetGoalId, session);
		
		// if it is course based goal
		CourseEnrollment invitedCourseEnrollment = targetGoalInvited.getCourseEnrollment();
		
		GoalStatus goalStatus = null;
		
		if (invitedCourseEnrollment != null) {
			CourseEnrollment loggedUserCourseEnrollment = courseManager.getCourseEnrollment(loggedUser, invitedCourseEnrollment.getCourse(), session);
			
			if (loggedUserCourseEnrollment != null) {
				if (loggedUserCourseEnrollment.getStatus().equals(Status.ACTIVE)) {
					goalStatus = GoalStatus.HAS_ACTIVE_COURSE_BASED_GOAL;
				} else if (loggedUserCourseEnrollment.getStatus().equals(Status.WITHDRAWN) && 
						loggedUserCourseEnrollment.getTargetGoal() != null) {
					goalStatus = GoalStatus.HAS_INACTIVE_COURSE_BASED_GOAL;
				}
			} 
			
			if (goalStatus == null) {
				goalStatus = GoalStatus.NO_GOAL_COURSE_CONNECTED;
			}
		} else {
			goalStatus = GoalStatus.NO_GOAL_REGULAR;
		}
		return goalStatus;
	}

}

