package org.prosolo.services.notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activities.requests.Request;
import org.prosolo.common.domainmodel.activities.requests.RequestStatus;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.common.domainmodel.workflow.evaluation.Evaluation;
import org.prosolo.common.domainmodel.workflow.evaluation.EvaluationSubmission;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.messaging.data.ServiceType;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.SuggestedLearningBean;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.util.RecommendationConverter;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.notifications.NotificationObserver")
public class NotificationObserver implements EventObserver {

	private static Logger logger = Logger.getLogger(NotificationObserver.class.getName());

	@Autowired private NotificationManager notificationsManager;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SuggestedLearningQueries suggestedLearningQueries;
	@Autowired private EvaluationUpdater evaluationUpdater;
	@Autowired private RecommendationConverter recommendationConverter;
	@Autowired private SessionMessageDistributer messageDistributer;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.JOIN_GOAL_REQUEST,
				EventType.JOIN_GOAL_REQUEST_APPROVED,
				EventType.JOIN_GOAL_REQUEST_DENIED,
				EventType.JOIN_GOAL_INVITATION,
				EventType.JOIN_GOAL_INVITATION_ACCEPTED,
				EventType.EVALUATION_REQUEST, 
				EventType.EVALUATION_ACCEPTED,
				EventType.EVALUATION_GIVEN, 
//				EventType.EVALUATION_EDITED, 
				EventType.Follow,
				EventType.ACTIVITY_REPORT_AVAILABLE,
				EventType.Comment,
				EventType.Like,
				EventType.Dislike,
				EventType.Post,
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();

		List<Notification> notifications = new ArrayList<Notification>();
		EventType action = event.getAction();
		
		try {
			BaseEntity object = event.getObject();
			
			User actor = event.getActor();
			if (object instanceof Request) {
				long requestId = object.getId();
				Request request = (Request) session.load(Request.class, requestId);
				request = HibernateUtil.initializeAndUnproxy(request);
	
				RequestStatus status = request.getStatus();
				User receiver = null;
	
				// this is the response to a request
				if (status.equals(RequestStatus.ACCEPTED) || status.equals(RequestStatus.DENIED)) {
					receiver = request.getMaker();
				} else {
					receiver = request.getSentTo();
				}
				
				if (event.getAction().equals(EventType.JOIN_GOAL_REQUEST_APPROVED)) {
					checkRecommendedationsForAcceptedLearningGoal(
							(TargetLearningGoal) request.getResource(), 
							receiver, 
							session);
				}
	
				// message
				String message = null;
	
				if (event.getAction().equals(EventType.JOIN_GOAL_INVITATION)
						|| event.getAction().equals(EventType.JOIN_GOAL_REQUEST)
						|| event.getAction().equals(EventType.EVALUATION_GIVEN)
						|| event.getAction().equals(EventType.EVALUATION_REQUEST)
//						|| event.getAction().equals(EventType.EVALUATION_EDITED)
					) {
					message = request.getComment();
				}
				Notification notification = notificationsManager.createNotification(
						request,
						actor, 
						receiver, 
						event.getAction(), 
						message,
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (object instanceof EvaluationSubmission) {
				EvaluationSubmission evSubmission = (EvaluationSubmission) object;	
				User receiver = evSubmission.getRequest().getMaker();

				Notification notification = notificationsManager.createNotification(
						evSubmission, 
						evSubmission.getMaker(), 
						receiver, 
						event.getAction(),
						evSubmission.getMessage(), 
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
	
				// if evaluatee is online, update number of evaluations for this resource
				long receiverId = receiver.getId();
				
				if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
					messageDistributer.distributeMessage(
						ServiceType.UPDATE_EVALUAIION_DATA,
						receiverId, 
						evSubmission.getId(), 
						null, 
						null);
				} else {
					HttpSession userSession = applicationBean.getUserSession(receiverId);
					
					if (userSession != null) {
						evaluationUpdater.updateEvaluationData(
							evSubmission.getId(),
							userSession,
							session);
					}
				}
			} else if (object instanceof Evaluation) {
				Evaluation evaluation = (Evaluation) object;
	
				BaseEntity resource = evaluation.getResource();
	
				long makerId = 0;
	
				if (resource instanceof CompletedResource) {
					makerId = ((CompletedResource) resource).getMaker().getId();
	
				} else if (resource instanceof TargetCompetence) {
					makerId = ((TargetCompetence) resource).getMaker().getId();
				}
	
				if (makerId > 0) {
					try {
						User receiver = defaultManager.loadResource(User.class, makerId, session);
					
						Notification notification = notificationsManager.createNotification(
								evaluation, 
								evaluation.getMaker(), 
								receiver,
								event.getAction(), 
								evaluation.getText(),
								event.getDateCreated(), 
								session);
						
						notifications.add(notification);
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error(e);
					}
				} else
					logger.error("Resoruce of type " + resource.getClass() + " is not supported for evaluations");
			} else if (object instanceof User) {
				if (action.equals(EventType.Follow)) {
					
					User follower = defaultManager.loadResource(User.class, actor.getId(), session);
					User userToFollow = defaultManager.loadResource(User.class, event.getObject().getId(), session);
					
					Notification notification = notificationsManager.createNotification(
							null, 
							follower, 
							userToFollow,
							event.getAction(), 
							null,
							event.getDateCreated(), 
							session);
					
					notifications.add(notification);
				}
			} else if (object instanceof Comment) {
				Comment comment = (Comment) object;
				
				if (action.equals(EventType.Comment) ) {
					BaseEntity commentedResource = comment.getObject();
					
					if (commentedResource instanceof SocialActivity) {
						User receiver = ((SocialActivity) commentedResource).getMaker();
						
						if (actor.getId() != receiver.getId()) {

							// generate notification about the comment
							Notification notification = notificationsManager.createNotification(
								comment,
								actor, 
								receiver, 
								event.getAction(), 
								comment.getText(),
								event.getDateCreated(), 
								session);
							
							notifications.add(notification);
						}
					} else if (commentedResource instanceof TargetActivity) {
						User receiver = ((TargetActivity) commentedResource).getMaker();
						
						if (actor.getId() != receiver.getId()) {
							
							// generate notification about the comment
							Notification notification = notificationsManager.createNotification(
								comment,
								actor, 
								receiver, 
								event.getAction(), 
								comment.getText(),
								event.getDateCreated(), 
								session);
							
							notifications.add(notification);
						}
					}
				}
			} else if (event.checkAction(EventType.ACTIVITY_REPORT_AVAILABLE)) {
				Notification notification = notificationsManager.createNotification(
						null, 
						null, 
						actor, 
						event.getAction(), 
						null, 
						event.getDateCreated(), 
						session);
				
				notifications.add(notification);
			} else if (event.checkAction(EventType.Like) || 
					event.checkAction(EventType.Dislike)) {
				
				if (object instanceof SocialActivity) {
					User receiver = ((SocialActivity) object).getMaker();
					
					if (actor.getId() != receiver.getId()) {
	
						// generate notification about the comment
						Notification notification = notificationsManager.createNotification(
							object,
							actor, 
							receiver, 
							event.getAction(), 
							null,
							event.getDateCreated(), 
							session);
						
						notifications.add(notification);
					}
				} else if (object instanceof TargetActivity) {
					User receiver = ((TargetActivity) object).getMaker();
					
					if (actor.getId() != receiver.getId()) {
						
						// generate notification about the comment
						Notification notification = notificationsManager.createNotification(
							object,
							actor, 
							receiver, 
							event.getAction(), 
							null,
							event.getDateCreated(), 
							session);
						
						notifications.add(notification);
					}
				}
	 
			} else if (event.checkAction(EventType.Post)) {
				Post post = (Post) object;
				
				if (post.getMentionedUsers() != null) {
					for (User user : post.getMentionedUsers()) {
						
						// generate notification about the comment
						Notification notification = notificationsManager.createNotification(
							post,
							actor, 
							user, 
							EventType.MENTIONED, 
							null,
							event.getDateCreated(), 
							session);
						
						notifications.add(notification);
					}
				}
			}
			session.flush();
			
			
			if (!notifications.isEmpty()) {
				
				for (Notification notification : notifications) {
					if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
						messageDistributer.distributeMessage(
								ServiceType.ADDNOTIFICATION, 
								notification.getReceiver().getId(),
								notification.getId(), 
								null, 
								null);
					} else {
						HttpSession httpSession = applicationBean.getUserSession(notification.getReceiver().getId());
					
						if (httpSession != null) {
							TopNotificationsBean topNotificationsBean = (TopNotificationsBean) httpSession.getAttribute("topNotificationsBean");
							LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession.getAttribute("loggeduser");
			
							if (topNotificationsBean != null) {
								try {
									notification = HibernateUtil.initializeAndUnproxy(notification);
									
									NotificationData notificationData = NotificationDataConverter.convertNotification(
											loggedUserBean.getUser(), 
											notification, 
											session, 
											loggedUserBean.getLocale());
									topNotificationsBean.addNotification(notificationData, session);
								} catch (NotificationNotSupported e) {
									logger.error(e);
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

	public void checkRecommendedationsForAcceptedLearningGoal(TargetLearningGoal targetGoal, User receiver, Session session) {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			messageDistributer.distributeMessage(
					ServiceType.CHECKRECOMMENDATIONSFORACCEPTEDLEARNINGGOAL,
					receiver.getId(), 
					targetGoal.getLearningGoal().getId(), 
					null, 
					null);
		} else {
			List<Recommendation> recommendations = suggestedLearningQueries.findSuggestedLearningResourcesForResource(receiver, targetGoal);
			ListIterator<Recommendation> recommendationIter = recommendations.listIterator();
			HttpSession userSession = applicationBean.getUserSession(receiver.getId());
			
			if (userSession != null) {
				
				SuggestedLearningBean userSuggestedLearningBean = (SuggestedLearningBean) userSession.getAttribute("suggestedLearningBean");
				
				while (recommendationIter.hasNext()) {
					Recommendation recommendation = recommendationIter.next();
					recommendation.setDismissed(true);
					session.saveOrUpdate(recommendation);
					
					if (userSuggestedLearningBean != null) {
						RecommendationData rData = recommendationConverter.convertRecommendationToRecommendedData(
								recommendation,
								session);
						userSuggestedLearningBean.removeSuggestedResource(rData);
					}
				}
			} 
		}
	}

}
