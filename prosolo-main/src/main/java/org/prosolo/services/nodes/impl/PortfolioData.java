/**
 * 
 */
package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.portfolio.CompletedGoal;
import org.prosolo.domainmodel.portfolio.ExternalCredit;

/**
 * @author nikolamilikic
 * 
 */
public class PortfolioData {

	private List<CompletedGoal> completedGoals;
	private List<AchievedCompetence> achievedCompetences;
	private List<ExternalCredit> externalCredits;
	
	public PortfolioData() {
		completedGoals = new ArrayList<CompletedGoal>();
		achievedCompetences = new ArrayList<AchievedCompetence>();
		externalCredits = new ArrayList<ExternalCredit>();
	}

	public List<CompletedGoal> getCompletedGoals() {
		return completedGoals;
	}

	public void setCompletedGoals(List<CompletedGoal> completedGoals) {
		this.completedGoals = completedGoals;
	}
	
	public boolean addCompletedGoal(CompletedGoal completedGoal) {
		if (completedGoal != null) {
			if (!completedGoals.contains(completedGoal)) {
				return completedGoals.add(completedGoal);
			}
		}
		return false;
	}

	public List<AchievedCompetence> getAchievedCompetences() {
		return achievedCompetences;
	}

	public void setAchievedCompetences(List<AchievedCompetence> achievedCompetences) {
		this.achievedCompetences = achievedCompetences;
	}
	
	public boolean addAchievedCompetence(AchievedCompetence achievedCompetence) {
		if (achievedCompetence != null) {
			if (!achievedCompetences.contains(achievedCompetence)) {
				return achievedCompetences.add(achievedCompetence);
			}
		}
		return false;
	}

	public List<ExternalCredit> getExternalCredits() {
		return externalCredits;
	}

	public void setExternalCredits(List<ExternalCredit> externalCredits) {
		this.externalCredits = externalCredits;
	}
	
	public boolean addExternalCredit(ExternalCredit externalCredit) {
		if (externalCredit != null) {
			if (!externalCredits.contains(externalCredit)) {
				return externalCredits.add(externalCredit);
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return completedGoals.isEmpty() && achievedCompetences.isEmpty() && externalCredits.isEmpty();
	}

}
