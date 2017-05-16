package org.prosolo.common.domainmodel.organization;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.general.BaseEntity;

/**
 * @author Bojan
 *
 * May 16, 2017
 */

@Entity
public class CompetenceUnit extends BaseEntity{

	private static final long serialVersionUID = 7719863079065072423L;
	
	private Competence1 competence;
	private Unit unit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	public Competence1 getCompetence(){
		return competence;
	}
	
	public void setCompetence(Competence1 competence){
		this.competence = competence;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public Unit getUnit(){
		return unit;
	}
	
	public void setUnit(Unit unit){
		this.unit = unit;
	}
}
