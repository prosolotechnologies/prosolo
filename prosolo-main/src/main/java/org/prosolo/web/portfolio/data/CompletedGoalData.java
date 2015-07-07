/**
 * 
 */
package org.prosolo.web.portfolio.data;

import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.web.data.GoalData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CompletedGoalData extends GoalData {
	
	private static final long serialVersionUID = -4130330192147557909L;
	
	private CompletedGoal completedGoal;
	
	public CompletedGoalData() {
		super();
	}

	public CompletedGoalData(TargetLearningGoal targetGoal) {
		super(targetGoal);
	}
	
	public CompletedGoal getCompletedGoal() {
		return completedGoal;
	}

	public void setCompletedGoal(CompletedGoal completedGoal) {
		this.completedGoal = completedGoal;
	}
	
}
