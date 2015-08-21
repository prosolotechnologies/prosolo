/**
 * 
 */
package org.prosolo.web.portfolio.util;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.BadgeManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.web.data.GoalData;
import org.prosolo.web.portfolio.data.CompletedGoalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.portfolio.util.CompletedGoalDataConverter")
public class CompletedGoalDataConverter {
	
	@Autowired private EvaluationManager evaluationManager;
	@Autowired private BadgeManager badgeManager;

	public List<GoalData> convertCompletedGoals(List<CompletedGoal> completedGoals) {
		List<GoalData> goalsData = new ArrayList<GoalData>();
		
		if (completedGoals != null && !completedGoals.isEmpty()) {
//			Collections.sort(completedGoals, new NodeCreatedDescComparator());
			
			for (CompletedGoal completedGoal : completedGoals) {
				goalsData.add(convertCompletedGoal(completedGoal));
			}
		}
		
		return goalsData;
	}
	
	public CompletedGoalData convertCompletedGoal(CompletedGoal completedGoal) {
		TargetLearningGoal targetGoal = completedGoal.getTargetGoal();
		
		CompletedGoalData completedGoalData = convertCompletedGoal(targetGoal);
		completedGoalData.setDateCompleted(completedGoal.getCompletedDate());
		completedGoalData.setDateCompletedString(DateUtil.getPrettyDate(completedGoal.getCompletedDate()));
		completedGoalData.setCompletedGoal(completedGoal);
		
		return completedGoalData;
	}
	
	public List<GoalData> convertTargetGoals(List<TargetLearningGoal> completedGoals) {
		List<GoalData> goalsData = new ArrayList<GoalData>();
		
		if (completedGoals != null && !completedGoals.isEmpty()) {
//			Collections.sort(completedGoals, new NodeCreatedDescComparator());
			
			for (TargetLearningGoal completedGoal : completedGoals) {
				goalsData.add(convertCompletedGoal(completedGoal));
			}
		}
		
		return goalsData;
	}

	public CompletedGoalData convertCompletedGoal(TargetLearningGoal targetGoal) {
		CompletedGoalData completedGoalData = new CompletedGoalData();
		
		completedGoalData.setTargetGoalId(targetGoal.getId());
		completedGoalData.setTitle(targetGoal.getLearningGoal().getTitle());
		completedGoalData.setDateStarted(targetGoal.getDateCreated());
		completedGoalData.setDateStartedString(DateUtil.getPrettyDate(targetGoal.getDateCreated()));
		completedGoalData.setVisibility(targetGoal.getVisibility());
		
		completedGoalData.setEvaluationCount(evaluationManager.getApprovedEvaluationCountForResource(TargetLearningGoal.class, targetGoal.getId()));
		completedGoalData.setRejectedEvaluationCount(evaluationManager.getRejectedEvaluationCountForResource(TargetLearningGoal.class, targetGoal.getId()));
		completedGoalData.setBadgeCount(badgeManager.getBadgeCountForResource(TargetLearningGoal.class, targetGoal.getId()));
		
		return completedGoalData;
	}

}
