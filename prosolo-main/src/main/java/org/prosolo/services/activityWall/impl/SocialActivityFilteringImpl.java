package org.prosolo.services.activityWall.impl;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.util.net.HttpSessionUtil;
import org.prosolo.services.activityWall.SocialActivityFilterProcessor;
import org.prosolo.services.activityWall.SocialActivityFiltering;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.strategy.StrategyFactory;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.ActivityWallBean;
import org.prosolo.web.goals.GoalWallBean;
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
	
	@Override
	public void checkSocialActivity(SocialActivity socialActivity) {
 
		Map<Long, HttpSession> httpsessions = applicationBean.getAllHttpSessions();
		Collection<HttpSession> sessions = httpsessions.values();
		
		for (HttpSession httpSession : sessions) {
			if (httpSession != null) {				
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession.getAttribute("loggeduser");
				User user = loggedUserBean.getUser();
				Filter selectedStatusWallFilter = loggedUserBean.getSelectedStatusWallFilter();
				SocialActivityFilterProcessor processor = strategyFactory.getFilterProcessor(SocialActivityFilterProcessor.class, selectedStatusWallFilter.getFilterType());
				boolean shouldBeDisplayedOnStatusWall=processor.checkSocialActivity(socialActivity, user, selectedStatusWallFilter);
				
				if (shouldBeDisplayedOnStatusWall) {
					ActivityWallBean activityWallBean = (ActivityWallBean) httpSession.getAttribute("activitywall");
					activityWallBean.getActivityWallDisplayer().addSocialActivity(socialActivity, user, loggedUserBean.getLocale(), true);
				}				
				Filter selectedGoalWallFilter = loggedUserBean.getSelectedLearningGoalFilter();
				
				if (selectedGoalWallFilter != null) {
					
					SocialActivityFilterProcessor goalWallProcessor = strategyFactory.getFilterProcessor(
							SocialActivityFilterProcessor.class,
							selectedGoalWallFilter.getFilterType());
					
					boolean shouldBeDisplayedOnGoalWall = goalWallProcessor.checkSocialActivity(socialActivity, user, selectedGoalWallFilter);
					
					if (shouldBeDisplayedOnGoalWall) {
						// Nikola changed this as Goal Wall activities are not stored now in session scoped LearningGoalsBean,
						// but in view scoped GoalWallBean
						
//						LearningGoalsBean goalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
//						GoalDataCache goalData = goalsBean.getSelectedGoalData();
//						goalData.addGoalWallActivity(socialActivityData);
						
//						LearningGoalsBean goalsBean = (LearningGoalsBean) httpSession.getAttribute("learninggoals");
						
						GoalWallBean goalWall =  (GoalWallBean) HttpSessionUtil.getAttributeEndsWith(httpSession, "goalwall");
						
						goalWall.getGoalWallDisplayer().addSocialActivity(socialActivity, user, loggedUserBean.getLocale(), true);
					} 
				}
			}
		}
	}
}

