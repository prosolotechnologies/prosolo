/**
 * 
 */
package org.prosolo.web.administration.data;

import java.io.Serializable;

import org.prosolo.domainmodel.admin.ResourceSettings;

/**
 * @author "Nikola Milikic"
 * 
 *         Sep 9, 2014
 */
public class ResourceSettingsData implements Serializable {

	private static final long serialVersionUID = -3125375213758410621L;

	private boolean userCanCreateCompetence;
	private boolean selectedUsersCanDoEvaluation;
	private boolean individualCompetencesCanNotBeEvaluated;
	
	// used when evaluating a goal
	private boolean goalAcceptanceDependendOnCompetence;
	
	private ResourceSettings settings;
	
	public ResourceSettingsData(ResourceSettings settings) {
		this.settings = settings;
		
		this.userCanCreateCompetence = settings.isUserCanCreateCompetence();
		this.selectedUsersCanDoEvaluation = settings.isSelectedUsersCanDoEvaluation();
		this.individualCompetencesCanNotBeEvaluated = settings.isIndividualCompetencesCanNotBeEvaluated();
		this.goalAcceptanceDependendOnCompetence = settings.isGoalAcceptanceDependendOnCompetence();
	}

	public boolean isUserCanCreateCompetence() {
		return userCanCreateCompetence;
	}

	public void setUserCanCreateCompetence(boolean userCanCreateCompetence) {
		this.userCanCreateCompetence = userCanCreateCompetence;
	}

	public boolean isSelectedUsersCanDoEvaluation() {
		return selectedUsersCanDoEvaluation;
	}

	public void setSelectedUsersCanDoEvaluation(
			boolean selectedUsersCanDoEvaluation) {
		this.selectedUsersCanDoEvaluation = selectedUsersCanDoEvaluation;
	}

	public boolean isGoalAcceptanceDependendOnCompetence() {
		return goalAcceptanceDependendOnCompetence;
	}

	public void setGoalAcceptanceDependendOnCompetence(
			boolean goalAcceptanceDependendOnCompetence) {
		this.goalAcceptanceDependendOnCompetence = goalAcceptanceDependendOnCompetence;
	}
	
	public boolean isIndividualCompetencesCanNotBeEvaluated() {
		return individualCompetencesCanNotBeEvaluated;
	}

	public void setIndividualCompetencesCanNotBeEvaluated(
			boolean individualCompetencesCanNotBeEvaluated) {
		this.individualCompetencesCanNotBeEvaluated = individualCompetencesCanNotBeEvaluated;
	}

	public ResourceSettings getSettings() {
		return settings;
	}

	public void setSettings(ResourceSettings settings) {
		this.settings = settings;
	}

	
	public void updateSettings() {
		this.settings.setUserCanCreateCompetence(this.userCanCreateCompetence);
		this.settings.setSelectedUsersCanDoEvaluation(this.selectedUsersCanDoEvaluation);
		this.settings.setIndividualCompetencesCanNotBeEvaluated(this.individualCompetencesCanNotBeEvaluated);
		this.settings.setGoalAcceptanceDependendOnCompetence(this.goalAcceptanceDependendOnCompetence);
	}

}
