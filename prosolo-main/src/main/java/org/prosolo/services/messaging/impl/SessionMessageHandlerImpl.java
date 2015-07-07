package org.prosolo.services.messaging.impl;

/**
 @author Zoran Jeremic Sep 9, 2014
 */
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.Notification;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.interfaceSettings.CommentUpdater;
import org.prosolo.services.interfaceSettings.LearnActivityCacheUpdater;
import org.prosolo.services.interfaceSettings.LearnPageCacheUpdater;
//import org.prosolo.services.interfaceSettings.SocialActivityCacheUpdater;
import org.prosolo.services.messaging.MessageHandler;
import org.prosolo.services.messaging.data.SessionMessage;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.notifications.EvaluationUpdater;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.courses.CoursePortfolioBean;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.home.SuggestedLearningBean;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.util.RecommendationConverter;
import org.prosolo.web.notification.TopInboxBean;
import org.prosolo.web.notification.TopNotificationsBean;
import org.prosolo.web.notification.data.NotificationData;
import org.prosolo.web.notification.exceptions.NotificationNotSupported;
import org.prosolo.web.notification.util.NotificationDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.prosolo.services.activitystream.SocialActivityInboxInterfaceCacheUpdater;

@Service("org.prosolo.services.messaging.SessionMessageHandler")
public class SessionMessageHandlerImpl implements MessageHandler<SessionMessage> {
	
	private static Logger logger = Logger .getLogger(SessionMessageHandlerImpl.class.getName());
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SuggestedLearningQueries suggestedLearningQueries;
	@Autowired private RecommendationConverter recommendationConverter;
	@Autowired private EvaluationUpdater evaluationUpdater;
	@Autowired private CommentUpdater commentUpdater;
	//@Autowired private SocialActivityCacheUpdater socialActivityCacheUpdater;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private LearnActivityCacheUpdater learnActivityCacheUpdaterImpl;
	//@Autowired private SocialActivityInboxInterfaceCacheUpdater socialActivityInboxInterfaceCacheUpdater;
	@Autowired private LearnPageCacheUpdater learnPageCacheUpdater;
	@Autowired private SocialActivityFiltering socialActivityFiltering;
	
	//@Autowired private SocialActivityResolver sActivityResolver;

	@Override
	public void handle(SessionMessage message) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		long receiverId = message.getReceiverId();
		HttpSession httpSession = applicationBean.getUserSession(receiverId);
		try{
			long resourceId = message.getResourceId();
			
			switch (message.getServiceType()) {
				case DIRECTMESSAGE:
					if (httpSession != null) {
						TopInboxBean inboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
						
						SimpleOfflineMessage directmessage = (SimpleOfflineMessage) session.load(
								SimpleOfflineMessage.class,
								resourceId);
						
						inboxBean.addNewUnreadMessage(directmessage);
					}
					break;
				case ADDNEWMESSAGETHREAD :
					if (httpSession != null) {
						// TODO: Nikola
//						MessagesThreadsBean messagesThreadBean = (MessagesThreadsBean) httpSession.getAttribute("messagesThreadsBean");
//						
//						MessagesThread messagesThread = (MessagesThread) session.load(
//								MessagesThread.class, 
//								resourceId);
//						
//						messagesThreadBean.addNewMessageThread(messagesThread);
					}
					break;
				case UPDATEMESSAGETHREAD:
					if (httpSession != null) {
						// TODO: Nikola
//						MessagesThreadsBean messagesThreadsBean1 = (MessagesThreadsBean) httpSession.getAttribute("messagesThreadsBean");
//						
//						MessagesThread messagesThread1 = (MessagesThread) session.load(
//								MessagesThread.class, 
//								resourceId);
//						
//						messagesThreadsBean1.addNewMessageThread(messagesThread1);
//						MessagesThreadBean messagesThreadBean1=(MessagesThreadBean) httpSession.getAttribute("messagesThreadBean");
//						
//						if (messagesThreadBean1 != null) {
//								messagesThreadBean1.refreshMessageThreadIfActive(messagesThread1.getId(), session);
//						}
					}
					break;
				case ADDNOTIFICATION:
					if (httpSession != null) {
						TopNotificationsBean topNotificationsBean = (TopNotificationsBean) httpSession.getAttribute("topNotificationsBean");
						LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession.getAttribute("loggeduser");
		
						if (topNotificationsBean != null) {
							try {
								Notification notification = (Notification) session.load(Notification.class, resourceId);
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
					break;
				case UPDATE_EVALUAIION_DATA:
					if (httpSession != null) {
						evaluationUpdater.updateEvaluationData(
								resourceId,
								httpSession, 
								session);
					}
					break;
				case CHECKRECOMMENDATIONSFORACCEPTEDLEARNINGGOAL:
					if (httpSession != null) {
						LearningGoal goal = (LearningGoal) session.load(LearningGoal.class, resourceId);
						User user = (User) session.load(User.class,	receiverId);
						
						List<Recommendation> recommendations = suggestedLearningQueries.findSuggestedLearningResourcesForResource(user, goal);
						SuggestedLearningBean userSuggestedLearningBean = (SuggestedLearningBean) httpSession.getAttribute("suggestedLearningBean");
	
						ListIterator<Recommendation> recommendationIter = recommendations.listIterator();
		
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
					break;
				case ADDSUGGESTEDBYCOLLEAGUES:
					if (httpSession != null) {
						SuggestedLearningBean suggestedLearningBean = (SuggestedLearningBean) httpSession.getAttribute("suggestedLearningBean");
					
						Recommendation recommendation = (Recommendation) session.load(
								Recommendation.class, 
								resourceId);
						
						RecommendationData rData = recommendationConverter.convertRecommendationToRecommendedData(
								recommendation,
								session);
						
						suggestedLearningBean.addSuggestedByColleagues(rData);
					}
					break;
				case ACCEPTJOINGOALNOTIFICATION:
					if (httpSession != null) {
						LearningGoal goal1 = (LearningGoal) session.load(LearningGoal.class, resourceId);
						LearningGoalsBean userLearningGoalBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
		
						if (userLearningGoalBean != null) {
							GoalDataCache goalData = userLearningGoalBean.getData().getDataForGoal(goal1);
		
							if (goalData != null) {
								goalData.setCollaborators(null);
								goalData.initializeCollaborators();
							}
						}
					}
					break;
				case UPDATEUSERSOCIALACTIVITYINBOX:
				//	boolean updateStatusWall = Boolean.parseBoolean(message.getParameters().get("updateStatusWall"));
				//	boolean updateGoalWall = Boolean.parseBoolean(message.getParameters().get("updateGoalWall"));
				//	boolean connectGoalNoteToStatus = Boolean.parseBoolean(message.getParameters().get("connectGoalNoteToStatus"));
					long socialActivityId = Long.parseLong(message.getParameters().get("socialActivityId"));
					
				//	List<Long> notifiedUsers = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					//Map<Long, HttpSession> sessionList = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
				//	if (receiverId > 0) {
				//		sessionList.put(receiverId, httpSession);
				//	} 
					// all logged in users should be updated
				//	else {
				//		sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers));
				//	}
					
//					SocialActivityNotification saNotification = null;
//					
//					if (resourceId > 0) {
//						saNotification = (SocialActivityNotification) session.load(SocialActivityNotification.class, resourceId);
//					}
					
					try {
						SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, socialActivityId, session);
						socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
						socialActivityFiltering.checkSocialActivity(socialActivity);
						//saNotification=(SocialActivityNotification) session.merge(saNotification);
						/*List<SocialStreamSubView> subViews = saNotification != null ? saNotification.getSubViews() : null;
						
						for (Entry<Long, HttpSession> sessionEntry : sessionList.entrySet()) {
								
							HttpSession userSession = sessionEntry.getValue();
							
							if (userSession != null) {
					 
								socialActivityHandler.updateUserSocialActivityInboxCache(
									sessionEntry.getKey(), 
									userSession, 
									saNotification, 
									socialActivity, 
									subViews, 
									updateStatusWall, 
									updateGoalWall, 
									connectGoalNoteToStatus,
									session);
							}
						}*/
						
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error(e);
					}
					break;
	/*			case UPDATEUSERSOCIALACTIVITYINBOX:
					boolean updateStatusWall = Boolean.parseBoolean(message.getParameters().get("updateStatusWall"));
					boolean updateGoalWall = Boolean.parseBoolean(message.getParameters().get("updateGoalWall"));
					boolean connectGoalNoteToStatus = Boolean.parseBoolean(message.getParameters().get("connectGoalNoteToStatus"));
					long socialActivityId = Long.parseLong(message.getParameters().get("socialActivityId"));
					
					List<Long> notifiedUsers = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					Map<Long, HttpSession> sessionList = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
					if (receiverId > 0) {
						sessionList.put(receiverId, httpSession);
					} 
					// all logged in users should be updated
					else {
						sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers));
					}
					
					SocialActivityNotification saNotification = null;
					
					if (resourceId > 0) {
						saNotification = (SocialActivityNotification) session.load(SocialActivityNotification.class, resourceId);
					}
					
					try {
						SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, socialActivityId, session);
						socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
						//saNotification=(SocialActivityNotification) session.merge(saNotification);
						List<SocialStreamSubView> subViews = saNotification != null ? saNotification.getSubViews() : null;
						
						for (Entry<Long, HttpSession> sessionEntry : sessionList.entrySet()) {
								
							HttpSession userSession = sessionEntry.getValue();
							
							if (userSession != null) {
					 
								socialActivityHandler.updateUserSocialActivityInboxCache(
									sessionEntry.getKey(), 
									userSession, 
									saNotification, 
									socialActivity, 
									subViews, 
									updateStatusWall, 
									updateGoalWall, 
									connectGoalNoteToStatus,
									session);
							}
						}
						
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error(e);
					}
					break;*/
				case ADD_COMMENT:
					long socialActivityId1 = Long.parseLong(message.getParameters().get("socialActivityId"));
					
					List<Long> notifiedUsers1 = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					Map<Long, HttpSession> sessionList = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
					if (receiverId > 0) {
						sessionList.put(receiverId, httpSession);
					} 
					// all logged in users should be updated
					else {
						sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers1));
					}
					
					Comment comment = defaultManager.loadResource(Comment.class, resourceId, session);
					
					for (Entry<Long, HttpSession> sessionEntry1 : sessionList.entrySet()) {
						HttpSession userSession1 = sessionEntry1.getValue();
						
						if (userSession1 != null) {
							commentUpdater.addCommentData(socialActivityId1, comment, userSession1);
						}
					}
					break;
				case UPDATE_COMMENT:
					long commentedResourceId = Long.parseLong(message.getParameters().get("commentedResourceId"));
					String commentedResourceClass = message.getParameters().get("commentedResourceClass");
					int likeCount = Integer.parseInt(message.getParameters().get("likeCount"));
					int dislikeCount = Integer.parseInt(message.getParameters().get("dislikeCount"));
					
					BaseEntity commentedResource = (BaseEntity) session.load(Class.forName(commentedResourceClass), commentedResourceId);
					
					List<Long> notifiedUsers3 = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					Map<Long, HttpSession> sessionList3 = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
					if (receiverId > 0) {
						sessionList3.put(receiverId, httpSession);
					} 
					// all logged in users should be updated
					else {
						sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers3));
					}
					
					Comment comment1 = defaultManager.loadResource(Comment.class, resourceId, session);
					comment1 = HibernateUtil.initializeAndUnproxy(comment1);
					
					for (Entry<Long, HttpSession> sessionEntry : sessionList3.entrySet()) {
						HttpSession userSession = sessionEntry.getValue();
						
						if (userSession != null) {
							System.out.println("UPDATING user: "+sessionEntry.getKey()+" comment "+resourceId+", parameters: "+message.getParameters());
							commentUpdater.updateCommentData(commentedResource, comment1, likeCount, dislikeCount, userSession);
						}
					}
					break;
				case REMOVE_TARGET_GOAL :
					if (httpSession != null) {
						LearningGoalsBean learningGoalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
						
						if (learningGoalsBean != null) {
							learningGoalsBean.getData().removeGoalByTargetId(resourceId);
						}
					}
					break;
				case ADD_ACTIVE_COURSE :
					if (httpSession != null) {
						CoursePortfolioBean coursePortfolioBean = (CoursePortfolioBean) httpSession.getAttribute("coursePortfolioBean");
						
						if (coursePortfolioBean != null) {
							coursePortfolioBean.removeFromWithdrawnCourses(resourceId);
							
							long enrollmentId = Long.parseLong(message.getParameters().get("enrollmentId"));
							CourseEnrollment enrollment = defaultManager.loadResource(CourseEnrollment.class, enrollmentId, session);
							
							if (enrollment != null) {
								coursePortfolioBean.addActiveCourse(enrollment);
							}
						}
					}
					break;
				case ADD_GOAL :
					if (httpSession != null) {
						LearningGoalsBean learningGoalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
						LoggedUserBean loggedUser = (LoggedUserBean) httpSession.getAttribute("loggeduser");
						
						if (learningGoalsBean != null) {
							TargetLearningGoal targetGoal = defaultManager.loadResource(TargetLearningGoal.class, resourceId, session);
							
							learningGoalsBean.getData().addGoal(loggedUser.getUser(), targetGoal, session);
						}
					}
					break;
				case UPDATE_SOCIAL_ACTIVITY:
					List<Long> notifiedUsers2 = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					Map<Long, HttpSession> sessionList2 = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
					if (receiverId > 0) {
						sessionList2.put(receiverId, httpSession);
					} 
					// all logged in users should be updated
					else {
						sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers2));
					}
					
					SocialActivity socialActivity = defaultManager.loadResource(SocialActivity.class, resourceId, session);
					socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
					
					for (Entry<Long, HttpSession> sessionEntry : sessionList2.entrySet()) {
						HttpSession userSession = sessionEntry.getValue();
						
						if (userSession != null) {
							socialActivityHandler.updateSocialActivity(socialActivity, userSession, session);
						}
					}
					break;
				case DELETE_SOCIAL_ACTIVITY:
					List<Long> notifiedUsers4 = StringUtils.fromCSV(message.getParameters().get("notifiedUsers"));
					Map<Long, HttpSession> sessionList4 = new HashMap<Long, HttpSession>();
					
					// only this specific user should be updated
					if (receiverId > 0) {
						sessionList4.put(receiverId, httpSession);
					} 
					// all logged in users should be updated
					else {
						sessionList = new HashMap<Long, HttpSession>(applicationBean.getHttpSessionsExcludingUsers(notifiedUsers4));
					}
					
					SocialActivity socialActivity4 = defaultManager.loadResource(SocialActivity.class, resourceId, session);
					socialActivity4 = HibernateUtil.initializeAndUnproxy(socialActivity4);
					
					for (Entry<Long, HttpSession> sessionEntry : sessionList4.entrySet()) {
						HttpSession userSession = sessionEntry.getValue();
						
						if (userSession != null) {
							socialActivityHandler.removeSocialActivity(socialActivity4, userSession, session);
						}
					}
					break;
				case REMOVE_GOAL_COLLABORATOR:
					if (httpSession != null) {
						long goalId = Long.parseLong(message.getParameters().get("learningGoal"));
						
						LearningGoal learningGoalToUpdate = defaultManager.loadResource(LearningGoal.class, goalId, session);
						User collaboratorToRemove = defaultManager.loadResource(User.class, resourceId, session);
						
						learnPageCacheUpdater.removeCollaboratorFormGoal(collaboratorToRemove, learningGoalToUpdate, httpSession);
					}
					break;
					
				case UPDATETARGETACTIVITYOUTCOME:
					if (httpSession != null) {
						long targetActivityId = Long.parseLong(message.getParameters().get("targetActivityId"));
						long outcomeId = Long.parseLong(message.getParameters().get("outcomeId"));
						Outcome outcome = defaultManager.loadResource(Outcome.class, outcomeId, true, session);
						
						learnActivityCacheUpdaterImpl.updateActivityOutcome(targetActivityId, outcome, httpSession, session);
					}
					break;
				default :
					break;
			}
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

}
