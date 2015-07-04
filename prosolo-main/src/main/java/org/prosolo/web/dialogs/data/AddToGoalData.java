/**
 * 
 */
package org.prosolo.web.dialogs.data;

import java.io.Serializable;

import org.prosolo.web.goals.cache.GoalDataCache;

/**
 * @author "Nikola Milikic"
 * 
 */
public class AddToGoalData implements Serializable {

	private static final long serialVersionUID = 671499454044098871L;

	private long goalId;
	private GoalDataCache goalData;;
	private boolean containsCompetence;

	public AddToGoalData(GoalDataCache goalData, boolean containsCompetence) {
		this.goalId = goalData.getData().getGoalId();
		this.goalData = goalData;
		this.containsCompetence = containsCompetence;
	}

	public long getGoalId() {
		return goalId;
	}

	public void setGoalId(long goalId) {
		this.goalId = goalId;
	}

	public GoalDataCache getGoalData() {
		return goalData;
	}

	public void setGoalData(GoalDataCache goalData) {
		this.goalData = goalData;
	}

	public boolean isContainsCompetence() {
		return containsCompetence;
	}

	public void setContainsCompetence(boolean containsCompetence) {
		this.containsCompetence = containsCompetence;
	}

}
