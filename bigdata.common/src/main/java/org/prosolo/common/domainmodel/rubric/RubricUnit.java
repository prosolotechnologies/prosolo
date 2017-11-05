package org.prosolo.common.domainmodel.rubric;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Unit;

import javax.persistence.*;

/**
 * @author Bojan
 *
 * May 16, 2017
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"rubric", "unit"})})
public class RubricUnit extends BaseEntity {

	private static final long serialVersionUID = -6845397842512255610L;

	private Rubric rubric;
	private Unit unit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Rubric getRubric(){
		return rubric;
	}
	
	public void setRubric(Rubric rubric){
		this.rubric = rubric;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Unit getUnit(){
		return unit;
	}
	
	public void setUnit(Unit unit){
		this.unit = unit;
	}
	

}
