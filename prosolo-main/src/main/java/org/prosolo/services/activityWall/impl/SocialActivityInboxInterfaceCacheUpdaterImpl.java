package org.prosolo.services.activityWall.impl;

/*import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activitywall.SocialActivity;
import org.prosolo.domainmodel.activitywall.SocialActivityNotification;
import org.prosolo.domainmodel.activitywall.SocialStreamSubView;
import org.prosolo.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.organization.VisibilityType;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.activitystream.FilterType;*/
import org.prosolo.services.activityWall.SocialActivityInboxInterfaceCacheUpdater;

@Deprecated
//@Service("org.prosolo.services.activitywall.SocialActivityInboxInterfaceCacheUpdater")
public class SocialActivityInboxInterfaceCacheUpdaterImpl implements SocialActivityInboxInterfaceCacheUpdater {
	/*
	private static Logger logger = Logger.getLogger(SocialActivityInboxInterfaceCacheUpdaterImpl.class);
	
	@Autowired private SessionMessageDistributer messageDistributer;
	@Autowired private DefaultManager defaultManager;
	
	@Transactional(readOnly = false)
	public void updateUserSocialActivityInboxCache(long userId, HttpSession userSession, 
			SocialActivityNotification saNotification, SocialActivity socialActivity, List<SocialStreamSubView> subViews, 
			boolean updateStatusWall, boolean updateGoalWall, boolean connectGoalNoteToStatus, Session session) {
		
		logger.debug("-------------------Updating social activity inbox interface cache of user "+userId+" for the social activity:" + socialActivity.getId()+", userSession");
		if(userSession==null){//during initialization this is always null
			return;
		}
		LoggedUserBean loggedUserBean = (LoggedUserBean) userSession.getAttribute("loggeduser");
		Locale locale = loggedUserBean.getLocale();
		
		if (updateStatusWall || connectGoalNoteToStatus) {
			ActivityWallBean activityWallBean = (ActivityWallBean) userSession.getAttribute("activitywall");
			
			if (activityWallBean != null) {
				FilterType filterSet = loggedUserBean.getInterfaceSettings().getActivityWallSettings().getChosenFilter();
				
				logger.debug("Current Status Wall filter is set to " + filterSet); 
				logger.debug("socialActivity is " + socialActivity); 

				EventType action = socialActivity.getAction();
				
				if (saNotification != null) {
					
					if (filterSet != null) {
						User actor = socialActivity.getActor();
						
						switch (filterSet) {
						case TWITTER:
							if (socialActivity instanceof TwitterPostSocialActivity) { 
								activityWallBean.addSocialActivity(locale, socialActivity, true);
							}
							break;
						case ALL:
							if (actor != null && !(socialActivity instanceof TwitterPostSocialActivity)) { 
								activityWallBean.addSocialActivityNotification(locale, saNotification);
							}
							break;
						case ALL_PROSOLO:
							if (actor != null && !action.equals(EventType.TwitterPost)) {
								activityWallBean.addSocialActivityNotification(locale, saNotification);
							}
							break;
						case MY_NETWORK:
							if (actor != null && actor.getId() != userId &&
									// to comment out if we want Twitter posts of my followers to go in
									!action.equals(EventType.TwitterPost)) {
								activityWallBean.addSocialActivityNotification(locale, saNotification);
							}
							break;
						case MY_ACTIVITIES:
							if (actor != null && actor.getId() == userId) {
								activityWallBean.addSocialActivityNotification(locale, saNotification);
							}
							break;
						default:
							break;
						}
					}
				} else {
					// this is public activity that should go to all users who have ALL filter on their Status Wall
					if (socialActivity.getVisibility().equals(VisibilityType.PUBLIC) && filterSet != null) {
						switch (filterSet) {
							case ALL:
								if (!(socialActivity instanceof TwitterPostSocialActivity)) { 
									activityWallBean.addSocialActivity(locale, socialActivity, true);
								}
								break;
							case ALL_PROSOLO:
									if (!action.equals(EventType.TwitterPost)) {
									activityWallBean.addSocialActivity(locale, socialActivity, true);
								}
								break;
						default:
							break;
						}
					}
				}
			}
		}
		
		// indicator this SocialActivity is directed to all users
		if (saNotification != null) {
			if (updateGoalWall && subViews != null) {
				GoalWallBean goalWallBean = (GoalWallBean) userSession.getAttribute("goalwall");
				LearningGoalsBean learningGoalsBean = (LearningGoalsBean) userSession.getAttribute("learninggoals");						
				
				if (learningGoalsBean != null && goalWallBean != null) {							
					for (SocialStreamSubView subView : subViews) {
						if (subView != null) {
							// find all Goals that are in related resources for this subview
							Set<Node> goals = subView.findRelatedResourceOfType(TargetLearningGoal.class);								
							
							for (Node tGoal : goals) {
								GoalDataCache goalData = learningGoalsBean.getData().getDataForTargetGoal(((TargetLearningGoal) tGoal).getId());									
								
								if (goalData != null)
									goalWallBean.addSocialActivityNotification(goalData, locale, saNotification);
							}
						}
					}
				}
			}
		}
	}
	*/
}
