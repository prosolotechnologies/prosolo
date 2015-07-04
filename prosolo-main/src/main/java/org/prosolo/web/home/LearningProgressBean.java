package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.goals.LearningGoalsBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.home.util.LastActivityComparatorDesc;
import org.prosolo.web.portfolio.PortfolioBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "learningProgress")
@Component("learningProgress")
@Scope("session")
public class LearningProgressBean implements Serializable{

	private static final long serialVersionUID = 2934819132229099781L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LearningProgressBean.class);
	
	@Autowired private LearningGoalsBean learningGoalsBean;
	@Autowired private ActivityWallManager activityWallManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioBean portfolioBean;
	
	private boolean initialized;
//	private List<GoalData> uncompletedGoals;
//	private List<TargetCompetenceData> uncompletedCompetences;
//	private int completedComNo;
//	private int totalCompNo;
	
	private int numberOfGoalsToDisplay = Settings.getInstance().config.application.home.learningProgress.numberOfGoals;
	private int numberOfCompToDisplay = Settings.getInstance().config.application.home.learningProgress.numberOfCompetences;
	
	public void initialize() {
		if (!initialized) {
			initializeData();
			
			initialized = true;
		}
	}

	public void initializeData() {
//		initilizeUncompletedGoals();
//		initilizeUncompletedCompetences();
//		initilizeCompletedCompNumber();
	}

//	private void initilizeUncompletedGoals() {
//		learningGoalsBean.initializeGoals();
//		uncompletedGoals = new ArrayList<GoalData>();
//		
//		if (learningGoalsBean.getData().getGoals() != null && !learningGoalsBean.getData().getGoals().isEmpty()) {
//			uncompletedGoals.addAll(learningGoalsBean.getOngoingGoals());
//			
////			for (GoalDataCache goalDataCache : learningGoalsBean.getData().getGoals()) {
////				GoalData goalData = goalDataCache.getData();
////				
////				if (goalData.getProgress() < 100) {
////					uncompletedGoals.add(goalData);
////				}
////			}
//		}
//		Collections.sort(uncompletedGoals, new UncompletedGoalDescComparator());
//	}
	
//	private void initilizeUncompletedCompetences() {
//		uncompletedCompetences = new LinkedList<TargetCompetenceData>();
//		
//		List<TargetCompetenceDateAggregate> tCompDateList = activityWallManager.getLatestUsedTargetCompetences(loggedUser.getUser(), numberOfCompToDisplay);
//	
//		if (tCompDateList != null && !tCompDateList.isEmpty()) {
//			for (TargetCompetenceDateAggregate targetCompetenceDateAggregate : tCompDateList) {
//				TargetCompetenceData data = new TargetCompetenceData(targetCompetenceDateAggregate.getTargetCompetence());
//				data.setLastActivity(targetCompetenceDateAggregate.getDate());
//				data.setTargetGoalId(targetCompetenceDateAggregate.getTargetCompetence().getParentGoal().getId());
//				
//				uncompletedCompetences.add(data);
//			}
//		}
//	}
	
//	private void initilizeCompletedCompNumber() {
//		completedComNo = activityWallManager.getCompletedCompetencesNumber(loggedUser.getUser());
//		totalCompNo = completedComNo + getUncompletedCompetences().size();
//	}

	/*
	 * GETTERS / SETTERS
	 */
	
	public List<GoalData> getOngoingGoals() {
		List<GoalData> goals = new ArrayList<GoalData>(learningGoalsBean.getOngoingGoals());
		
		Collections.sort(goals, new LastActivityComparatorDesc());
		
		return goals;
	}
	
	public List<GoalData> getOngoingGoalsShown() {
		if (getOngoingGoals().size() > numberOfGoalsToDisplay) {
			return getOngoingGoals().subList(0, numberOfGoalsToDisplay);
		} else {
			return getOngoingGoals();
		}
	}
	
	public List<GoalData> getOngoingGoalsHidden() {
		if (getOngoingGoals().size() > numberOfGoalsToDisplay) {
			return getOngoingGoals().subList(numberOfGoalsToDisplay, getOngoingGoals().size());
		} else {
			return new ArrayList<GoalData>();
		}
	}
	
	public List<CompetenceDataCache> getOngoingCompetences() {
		List<CompetenceDataCache> ongoingCompetences = learningGoalsBean.getOngoingCompetences();
		
		Collections.sort(ongoingCompetences, new LastActivityComparatorDesc());
		
		return ongoingCompetences;
	}

	public int getCompletedComNo() {
		return portfolioBean.getCompletedComps().size();
	}

	public int getTotalCompNo() {
		return getCompletedComNo() + getOngoingCompetences().size();
	}
	
	public String getCompetenceProgress(){
		if (getTotalCompNo() == 0) {
			return "0";
		} else {
			double c = ((double) getCompletedComNo()) / getTotalCompNo();
			return String.valueOf(Math.round(c * 100));
		}
	}

	public int getNumberOfGoalsToDisplay() {
		return numberOfGoalsToDisplay;
	}

	public int getNumberOfCompToDisplay() {
		return numberOfCompToDisplay;
	}
	
}
