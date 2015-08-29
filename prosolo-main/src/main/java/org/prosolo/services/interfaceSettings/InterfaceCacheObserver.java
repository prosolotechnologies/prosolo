package org.prosolo.services.interfaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.util.StringUtils;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.portfolio.PortfolioBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.interfaceSettings.InterfaceCacheUpdater")
public class InterfaceCacheObserver implements EventObserver {
	
	private static Logger logger = Logger.getLogger(InterfaceCacheObserver.class);
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private ActivityManager activityManager;
	@Autowired private CommentUpdater commentUpdater;
	@Autowired private SocialActivityHandler socialActivityHandler;
	@Autowired private LearnPageCacheUpdater learnPageCacheUpdater;
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private LearningGoalManager goalManager;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
			EventType.Delete,
			EventType.Detach,
			EventType.CommentsEnabled,
			EventType.CommentsDisabled,
			EventType.Like,
			EventType.RemoveLike,
			EventType.Dislike,
			EventType.RemoveDislike,
			EventType.Comment,
			EventType.PostShare,
			EventType.PostUpdate,
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { 
			SocialActivity.class,
			Comment.class,
			LearningGoal.class,
			Post.class
		};
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) goalManager.getPersistence().openSession();
		
		try {
			EventType action = event.getAction();
			User actor = event.getActor();
			BaseEntity object = event.getObject();
			BaseEntity target = event.getTarget();
			
			if (target != null) {
				target = HibernateUtil.initializeAndUnproxy(target);
			}
			
			if (object instanceof SocialActivity) {
				SocialActivity socialActivity = (SocialActivity) object;
				
				if (action.equals(EventType.Delete) && object instanceof SocialActivity) {
					deleteSocialActivityFromCachesOfOnlineUsers(socialActivity, actor, session);
					
					// if it was a reshare of a post, then update share count 
					if (socialActivity instanceof PostSocialActivity) {
						Post post = ((PostSocialActivity) socialActivity).getPostObject();
						
						Post originalPost = post.getReshareOf();
						
						if (originalPost != null) {
							SocialActivity socialActivityOfOriginalPost = activityWallManager.getSocialActivityOfPost(originalPost, session);
							
							if (socialActivityOfOriginalPost != null) {
								updateSocialActivityInCachesOfOnlineUsers(socialActivityOfOriginalPost, actor, session);
							}
						}
					}
					
				// comments must come before SocialActivity as Comment is subclass of SocialActivity
				} else if (object instanceof Comment) {
					Comment comment = (Comment) object;
					
					if (action.equals(EventType.Comment)) {
						BaseEntity commentedResource = comment.getObject();
						
						if (commentedResource instanceof SocialActivity) {
							addCommentToSocialActiviesInCachesOfOnlineUsers(
									comment, 
									(SocialActivity) commentedResource, 
									actor,
									session);
						} else if (commentedResource instanceof TargetActivity) {
							updateCommentsOfActivityInCachesOfOnlineUsers(
									comment, 
									(TargetActivity) commentedResource, 
									actor,
									session);
						}
					} else if (action.equals(EventType.Like) || 
							action.equals(EventType.RemoveLike) ||
							action.equals(EventType.Dislike) || 
							action.equals(EventType.RemoveDislike)) {
						updateCommentDataInCachesOfOnlineUsers(comment, actor, session);
					}
				} else if (object instanceof SocialActivity && 
						(action.equals(EventType.CommentsEnabled) || 
								action.equals(EventType.CommentsDisabled) || 
								action.equals(EventType.Like) || 
								action.equals(EventType.RemoveLike) ||
								action.equals(EventType.Dislike) || 
								action.equals(EventType.PostUpdate) || 
								action.equals(EventType.RemoveDislike))){
					
					updateSocialActivityInCachesOfOnlineUsers(socialActivity, actor, session);
				} else if (object instanceof LearningGoal && action.equals(EventType.Detach)) {
					updateAfterGoalDeleted((LearningGoal) object, actor, session);
				} 
			} else if (action.equals(EventType.PostShare)) {
				Map<String, String> params = event.getParameters();
				
				SocialActivity originalSocialActivity = activityManager.loadResource(SocialActivity.class, Long.parseLong(params.get("originalSocialActivityId")));
				
				updateSocialActivityInCachesOfOnlineUsers(originalSocialActivity, actor, session);
			}
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}

	private void updateSocialActivityInCachesOfOnlineUsers(SocialActivity socialActivity, User actor, Session session) {
		List<User> usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity(socialActivity, session);
		usersSubscribedToEvent.remove(actor);
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				messageDistributer.distributeMessage(
						ServiceType.UPDATE_SOCIAL_ACTIVITY,
						u.getId(), 
						socialActivity.getId(), 
						null, 
						null);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				if (httpSession != null) {
					socialActivityHandler.updateSocialActivity(socialActivity, httpSession, session);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		List<Long> notifiedUserIds = new ArrayList<Long>();
		
		notifiedUserIds.add(actor.getId());
		
    	for (User u : usersSubscribedToEvent) {
    		notifiedUserIds.add(u.getId());
		}
    	
    	if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.UPDATE_SOCIAL_ACTIVITY,
					-1, // setting -1 for user id means all users should be updated
					socialActivity.getId(), 
					null,
					parameters);
    	}
	}
	
	/**
	 * Goes through all online users' sessions and removes the wall activity data
	 * 
	 * @param socialActivity
	 */
	private void deleteSocialActivityFromCachesOfOnlineUsers(SocialActivity socialActivity, User actor, Session session) {
		List<User> usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity(socialActivity, session);
		usersSubscribedToEvent.remove(actor);
		
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				messageDistributer.distributeMessage(
						ServiceType.DELETE_SOCIAL_ACTIVITY,
						u.getId(), 
						socialActivity.getId(), 
						null, 
						null);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				if (httpSession != null) {
					socialActivityHandler.removeSocialActivity(socialActivity, httpSession, session);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		List<Long> notifiedUserIds = new ArrayList<Long>();
		
		notifiedUserIds.add(actor.getId());
		
    	for (User u : usersSubscribedToEvent) {
    		notifiedUserIds.add(u.getId());
		}
    	
    	if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.DELETE_SOCIAL_ACTIVITY,
					-1, // setting -1 for user id means all users should be updated
					socialActivity.getId(), 
					null,
					parameters);
    	}
	}
	
	private void addCommentToSocialActiviesInCachesOfOnlineUsers(Comment comment, SocialActivity socialActivity, User user, Session session) {
		List<User> usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity(socialActivity);
		usersSubscribedToEvent.remove(user);
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("socialActivityId", String.valueOf(socialActivity.getId()));
				
				messageDistributer.distributeMessage(
						ServiceType.ADD_COMMENT,
						u.getId(), 
						comment.getId(), 
						null, 
						parameters);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				try {
					commentUpdater.addCommentData(
							socialActivity.getId(), 
							comment, 
							httpSession);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			List<Long> notifiedUserIds = new ArrayList<Long>();
			
			notifiedUserIds.add(user.getId());
			
	    	for (User u : usersSubscribedToEvent) {
	    		notifiedUserIds.add(u.getId());
			}
    	
	    	Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("socialActivityId", String.valueOf(socialActivity.getId())); 
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds)); 
			
			messageDistributer.distributeMessage(
					ServiceType.ADD_COMMENT,
					-1, // setting -1 for user id means all users should be updated
					0,
					null,
					parameters);
    	}
	}
	
	private void updateCommentsOfActivityInCachesOfOnlineUsers(Comment comment, TargetActivity activity, User user, Session session) {
		List<User> usersSubscribedToEvent = activityManager.getUsersHavingTargetActivityInLearningGoal(activity, session);
		usersSubscribedToEvent.remove(user);
		
		List<HttpSession> usersSessions = applicationBean.getHttpSessionsOfUsers(usersSubscribedToEvent);
		
    	for (HttpSession httpSession : usersSessions) {
			LearningGoalsBean learningGoalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().addCommentToActivity(activity.getId(), comment);
			}
		}
	}
	
	private void updateCommentDataInCachesOfOnlineUsers(Comment comment, User user, Session session) {
		BaseEntity commentedRes = comment.getObject();
		
		commentedRes = (BaseEntity) session.merge(commentedRes);
		commentedRes = HibernateUtil.initializeAndUnproxy(commentedRes);
		
		int commentLikeCount = likeManager.likeCount(comment, session);
		int commentDislikeCount = dislikeManager.dislikeCount(comment, session);
		
		List<User> usersSubscribedToEvent = new ArrayList<User>();
		
		if (commentedRes instanceof TargetActivity) {
			usersSubscribedToEvent = activityManager.getUsersHavingTargetActivityInLearningGoal((TargetActivity) commentedRes, session);
		} else if (commentedRes instanceof SocialActivity) {
			usersSubscribedToEvent = activityWallManager.getUsersSubscribedToSocialActivity((SocialActivity) commentedRes, session);
		}
		
		usersSubscribedToEvent.remove(user);
		
		for (User u : usersSubscribedToEvent) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("commentedResourceId", String.valueOf(commentedRes.getId()));
				parameters.put("commentedResourceClass", commentedRes.getClass().getName());
				parameters.put("likeCount", String.valueOf(commentLikeCount));
				parameters.put("dislikeCount", String.valueOf(commentDislikeCount));
				
				System.out.println("Sending UPDATE_COMMENT to user " + u.getId() + ", commentId: "+comment.getId()+", parameters: "+parameters);
				
				messageDistributer.distributeMessage(
						ServiceType.UPDATE_COMMENT,
						u.getId(), 
						comment.getId(), 
						null, 
						parameters);
			} else {
				HttpSession httpSession = applicationBean.getUserSession(u.getId());
				
				try {
					commentUpdater.updateCommentData(commentedRes, comment, commentLikeCount, commentDislikeCount, httpSession);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
				}
			}
		}
		
		// update caches of all users who have ALL or ALL_PROSOLO filter set on their Status Wall
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
			List<Long> notifiedUserIds = new ArrayList<Long>();
			
			notifiedUserIds.add(user.getId());
			
	    	for (User u : usersSubscribedToEvent) {
	    		notifiedUserIds.add(u.getId());
			}
    	
    		Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("commentedResourceId", String.valueOf(commentedRes.getId()));
			parameters.put("commentedResourceClass", commentedRes.getClass().getName());
			parameters.put("likeCount", String.valueOf(commentLikeCount));
			parameters.put("dislikeCount", String.valueOf(commentDislikeCount));
			parameters.put("notifiedUsers", StringUtils.toCSV(notifiedUserIds));
			
			System.out.println("Sending UPDATE_COMMENT to ALL USERS except" + notifiedUserIds + ", commentId: "+comment.getId()+", parameters: "+parameters);
			
			messageDistributer.distributeMessage(
					ServiceType.UPDATE_COMMENT,
					-1, // setting -1 for user id means all users should be updated
					comment.getId(), 
					null,
					parameters);
    	}
	}
	
	private void updateAfterGoalDeleted(LearningGoal goal, User actor, Session session) {
		// update Portfolio cache of online user if exists
    	final PortfolioBean portfolioBean = (PortfolioBean) applicationBean.getUserSession(actor.getId()).getAttribute("portfolio");
    	
    	if (portfolioBean != null)
    		portfolioBean.populateWithActiveCompletedCompetences();
    	
    	
    	// update collaborator list of goal's members
    	goal = activityManager.merge(goal, session);
    	List<User> collaborators = goalManager.retrieveCollaborators(goal.getId(), actor, session);
    	
    	Iterator<User> iterator = collaborators.iterator();
		
		while (iterator.hasNext()) {
			User user = (User) iterator.next();
			
			if (user.getId() == actor.getId()) {
				iterator.remove();
				break;
			}
		}
    	
    	for (User user : collaborators) {
			if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
				
    			Map<String, String> parameters = new HashMap<String, String>();
				parameters.put("learningGoal", String.valueOf(goal.getId()));
				
				messageDistributer.distributeMessage(
						ServiceType.REMOVE_GOAL_COLLABORATOR,
						user.getId(), 
						actor.getId(), 
						null, 
						parameters);
			} else {
				HttpSession userSession = applicationBean.getUserSession(user.getId());
				
				learnPageCacheUpdater.removeCollaboratorFormGoal(actor, goal, userSession);
			}
		}
	}

	public void asyncResetGoalCollaborators(final long goalId, final User user) { 
		taskExecutor.execute(new Runnable() {
		    @Override
		    public void run() {
		    	Session session = (Session) goalManager.getPersistence().openSession();
		    	
		    	try {
			    	List<User> collaborators = goalManager.retrieveCollaborators(goalId, user, session);
			    	
			    	for (User user : collaborators) {
				    	HttpSession userSession = applicationBean.getUserSession(user.getId());
			    		
						if (CommonSettings.getInstance().config.rabbitMQConfig.distributed) {
							messageDistributer.distributeMessage(
									ServiceType.ACCEPTJOINGOALNOTIFICATION,
									user.getId(), 
									goalId, 
									null, 
									null);
						} else if (userSession != null) {
			    			LearningGoalsBean userLearningGoalBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
							
							if (userLearningGoalBean != null) {
								GoalDataCache goalData = userLearningGoalBean.getData().getDataForGoal(goalId);
								
								if (goalData != null) {
									goalData.setCollaborators(null);
									goalData.initializeCollaborators();
								}
							}
						} 
			    	}
			    } catch (Exception e) {
					logger.error(e);
				} finally {
					HibernateUtil.close(session);
				}
		    }
		});
	}

}
