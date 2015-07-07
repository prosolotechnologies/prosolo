package org.prosolo.services.activityWall.impl;


import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallFactory;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.SocialActivityFactory;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.activityWall.SocialActivityHandler;
import org.prosolo.services.activityWall.SocialActivityResolver;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventException;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.goals.GoalWallBean;
import org.prosolo.web.goals.LearningGoalsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


/**
@author Zoran Jeremic Jan 16, 2015
 *
 */
@Service("org.prosolo.services.activitywall.SocialActivityHandler")
public class SocialActivityHandlerImpl implements SocialActivityHandler{

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SocialActivityHandler.class);
	
	@Autowired private SocialActivityFactory socialActivityFactory;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private SocialActivityResolver sActivityResolver;
	@Autowired private ActivityWallFactory activityWallFactory;
	@Autowired private DefaultManager defaultManager;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired private SocialActivityFiltering socialActivityFiltering;
	
	
	@Override
	public SocialActivity addSociaActivitySyncAndPropagateToStatusAndGoalWall(Event event) throws EventException {
		System.out.println("ADD SOCIAL ACTIVITY SYNC AND PROPAGATE TO STATUS AND GOAL WALL");
		Session session = (Session) defaultManager.getPersistence().currentManager();
		final SocialActivity socialActivity = socialActivityFactory.createSocialActivity(event, session, null);
		socialActivityFiltering.checkSocialActivity(socialActivity);
		return socialActivity;
	}
	
	@Override
	public void addSociaActivitySyncAndPropagateToGoalWall(Event event, GoalWallBean goalWallBean, User user, Locale locale) throws EventException {
		System.out.println("ADD SOCIAL ACTIVITY SYNC AND PROPAGATE TO GOAL WALL");
		Session session = (Session) defaultManager.getPersistence().currentManager();
		SocialActivity socialActivity = socialActivityFactory.createSocialActivity(event, session, null);
		
//		SocialActivityNotification saNotification = socialActivityFactory.createSocialActivityNotification(user, socialActivity, subViews, true, session);
//		SocialActivityWallData wallData = wallActivityConverter.convertSocialActivityNotification(saNotification, user, SocialStreamSubViewType.GOAL_WALL, locale);
//		wallData.setWallOwner(new UserData(user));
//		if (goalData != null) {
// 			wallData.setGoal(goalData.getData().getAsNodeData());
// 		}
//		wallData.setWallOwner(new UserData(user));
		
		SocialActivityData socialActivityData = wallActivityConverter.convertSocialActivityToSocialActivityData(socialActivity, user, SocialStreamSubViewType.GOAL_WALL, locale);
		// add to goal wall
		
 		if (goalWallBean != null) {
 			goalWallBean.getGoalWallDisplayer().addNewWallActivity(socialActivityData);
 		}
	}
 //	Session session = (Session) defaultManager.getPersistence().currentManager();
//		
//		final SocialActivity socialActivity = socialActivityFactory.createSocialActivity(event, session, null);
//		List<SocialStreamSubView> subViews = new ArrayList<SocialStreamSubView>();
//
//		if (postToStatusWall) {
//			subViews.add(activityWallFactory.createStatusWallSubView(null, session));
//		}
//		if (postToGoalWall) {
//			// several goals can be interested in the same hashtag and thus in this SocialActivity
 //			subViews.addAll(activityWallFactory.createGoalWallSubViews(user, socialActivity, null, session));
//		}
//		SocialActivityNotification saNotification = socialActivityFactory.createSocialActivityNotification(user, socialActivity, subViews, true, session);
//		
//		SocialActivityWallData wallData = wallActivityConverter.convertSocialActivityNotification(saNotification, user, SocialStreamSubViewType.GOAL_WALL, locale);
//		
//		if (goalData != null) {
//			wallData.setGoal(goalData.getData().getAsNodeData());
//		}
//		wallData.setWallOwner(new UserData(user));
//		
//		// add to status wall
////		if (postToStatusWall && activityWallBean != null ) {
////			activityWallBean.addWallActivity(wallData);
////		}
//		
//		// add to goal wall
//		if (goalData != null) {
//			goalData.addGoalWallActivity(wallData);
//		}
//		taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//            	Session session = (Session) defaultManager.getPersistence().openSession();
//				try {
//	            	updateUserSocialActivityInboxes(socialActivity, false, session);
//	            	session.flush();
//				} catch (Exception e) {
//					logger.error("Exception in handling message", e);
//				} finally {
//					HibernateUtil.close(session);
//				}
//            }
//        });
//		return wallData;
	//}
	@Override
	public void propagateSocialActivity(Event event) throws EventException {
			System.out.println("propagateSocialActivity");
		  addSociaActivitySyncAndPropagateToStatusAndGoalWall(event);
	}
	
	@Override
	public void updateSocialActivity(SocialActivity socialActivity, HttpSession userSession, Session session) {
		if (userSession != null) {
			LearningGoalsBean learningGoalsBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
			
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().updateSocialActivity(socialActivity);
			}
			
			ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
			
			if (activityWallBean != null) {
				activityWallBean.getActivityWallDisplayer().updateSocialActivity(socialActivity);
			}
		}
	}
	
	@Override
	public void disableSharing(SocialActivity socialActivity, HttpSession userSession, Session currentManager) {
		if (userSession != null) {
			LearningGoalsBean learningGoalsBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
			
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().disableSharing(socialActivity);
			}
			
			ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
			
			if (activityWallBean != null) {
				activityWallBean.getActivityWallDisplayer().disableSharing(socialActivity);
			}
		}
	}

	@Override
	public void removeSocialActivity(SocialActivity socialActivity, HttpSession userSession, Session session) {
		if (userSession != null) {
			LearningGoalsBean learningGoalsBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");
			
			if (learningGoalsBean != null) {
				learningGoalsBean.getData().removeWallActivityData(socialActivity.getId());
			}
			
			ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
			
			if (activityWallBean != null) {
				activityWallBean.getActivityWallDisplayer().removeSocialActivity(socialActivity.getId());
			}
		}
	}
	
}
