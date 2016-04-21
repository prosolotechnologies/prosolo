/**
 * 
 */
package org.prosolo.common.domainmodel.evaluation;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class TargetCompetenceEvaluation extends Evaluation {

	private static final long serialVersionUID = -7326473350379322512L;

	private TargetCompetence targetCompetence;
	
	@OneToOne
	public TargetCompetence getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	@Override
	@Transient
	public TargetCompetence getResource() {
		return targetCompetence;
	}
	
	public void setResource(BaseEntity resource) {
		targetCompetence = (TargetCompetence) resource;
	}
	
}
