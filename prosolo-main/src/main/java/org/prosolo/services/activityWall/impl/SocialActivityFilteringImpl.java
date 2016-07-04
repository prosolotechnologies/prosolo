package org.prosolo.services.activityWall.impl;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.strategy.StrategyFactory;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
@author Zoran Jeremic Jan 25, 2015
 *
 */
@Service("org.prosolo.services.activitywall.SocialActivityFiltering")
public class SocialActivityFilteringImpl implements SocialActivityFiltering {
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private StrategyFactory strategyFactory;
	@Inject private SocialActivityManager socialActivityManager;
	
	@Override
	public void checkSocialActivity(SocialActivity1 socialActivity, Session session) {
		
		Map<Long, HttpSession> httpsessions = applicationBean.getAllHttpSessions();
		Collection<HttpSession> sessions = httpsessions.values();
		/*
		 * clear session because we need to get full social activity data and strange behaviour
		 * occurs when we already have same data in session
		 */
		session.clear();
		//TODO test test delete
//		SocialActivityData1 socialActivityData1 = socialActivityManager
//				.getSocialActivity(socialActivity.getId(), socialActivity.getClass(), 
//						2, new Locale("en", "US"), session);
		
		for (HttpSession httpSession : sessions) {
			if (httpSession != null) {				
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession.getAttribute("loggeduser");
				User user = loggedUserBean.getUser();
				//TODO check if it workds - if it is post social activity it is already added to creator's wall when he created post.
				if(socialActivity instanceof PostSocialActivity1 
						&& socialActivity.getActor().getId() == user.getId()) {
					continue;
				}
				Filter selectedStatusWallFilter = loggedUserBean.getSelectedStatusWallFilter();
				SocialActivityFilterProcessor processor = strategyFactory.getFilterProcessor(
						SocialActivityFilterProcessor.class, selectedStatusWallFilter.getFilterType());
				boolean shouldBeDisplayedOnStatusWall=processor.checkSocialActivity(socialActivity, user, 
						selectedStatusWallFilter);
				
				if (shouldBeDisplayedOnStatusWall) {
					ActivityWallBean activityWallBean = (ActivityWallBean) httpSession.getAttribute("activityWallBean");
					if(activityWallBean!=null){
						SocialActivityData1 socialActivityData = socialActivityManager
								.getSocialActivity(socialActivity.getId(), socialActivity.getClass(), 
										loggedUserBean.getUser().getId(), loggedUserBean.getLocale(), session);
						activityWallBean.addNewSocialActivity(socialActivityData);
					}

				}		
				//TODO commented fragment of code
//				Filter selectedGoalWallFilter = loggedUserBean.getSelectedLearningGoalFilter();
//				
//				if (selectedGoalWallFilter != null) {
//					
//					SocialActivityFilterProcessor goalWallProcessor = strategyFactory.getFilterProcessor(
//							SocialActivityFilterProcessor.class,
//							selectedGoalWallFilter.getFilterType());
//					
//					boolean shouldBeDisplayedOnGoalWall = goalWallProcessor.checkSocialActivity(socialActivity, user, selectedGoalWallFilter);
//					
//					if (shouldBeDisplayedOnGoalWall) {
//						// Nikola changed this as Goal Wall activities are not stored now in session scoped LearningGoalsBean,
//						// but in view scoped GoalWallBean
//						
////						LearningGoalsBean goalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
////						GoalDataCache goalData = goalsBean.getSelectedGoalData();
////						goalData.addGoalWallActivity(socialActivityData);
//						
////						LearningGoalsBean goalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
//						
//						GoalWallBean goalWall =  (GoalWallBean) HttpSessionUtil.getAttributeEndsWith(httpSession, "goalwall");
//						
//						goalWall.getGoalWallDisplayer().addSocialActivity(socialActivity, user, loggedUserBean.getLocale(), true);
//					} 
//				}
			}
		}
	}
}

