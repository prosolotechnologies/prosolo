/**
 * 
 */
package org.prosolo.common.domainmodel.evaluation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class AchievedCompetenceEvaluation extends Evaluation {

	private static final long serialVersionUID = -6525685297788208735L;

	private AchievedCompetence achievedCompetence;
	
	@OneToOne
	public AchievedCompetence getAchievedCompetence() {
		return achievedCompetence;
	}

	public void setAchievedCompetence(AchievedCompetence achievedCompetence) {
		this.achievedCompetence = achievedCompetence;
	}

	@Override
	@Transient
	public AchievedCompetence getResource() {
		return achievedCompetence;
	}
	
	public void setResource(BaseEntity resource) {
		achievedCompetence = (AchievedCompetence) resource;
	}
	
}
