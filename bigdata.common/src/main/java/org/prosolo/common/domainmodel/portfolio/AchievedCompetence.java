/**
 * 
 */
package org.prosolo.common.domainmodel.portfolio;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
////@Table(name="portfolio_AchievedCompetence")
public class AchievedCompetence extends CompletedResource {
	
	private static final long serialVersionUID = -2125619051783878523L;
	
	private TargetCompetence targetCompetence;
	private Competence competence;
	
	@OneToOne
	public TargetCompetence getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	@OneToOne
	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}
	
	@Override
	@Transient
	public BaseEntity getResource() {
		return targetCompetence;
	}

}
