package org.prosolo.common.domainmodel.organization;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.rubric.RubricUnit;

import javax.persistence.*;
import java.util.List;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
//unique constraint added from the script
public class Unit extends BaseEntity {

	private static final long serialVersionUID = 6378214271994742348L;
	
	private Organization organization;
	private List<UnitRoleMembership> unitRoleMemberships;
	private List<CredentialUnit> credentialUnits;
	private List<CompetenceUnit> competenceUnits;
	private List<RubricUnit> rubricUnits;
	private Unit parentUnit;
	private String welcomeMessage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Organization getOrganization(){
		return organization;
	}
	
	public void setOrganization(Organization organization){
		this.organization = organization;
	}

	@OneToMany(mappedBy = "unit", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<UnitRoleMembership> getUnitRoleMemberships(){
		return unitRoleMemberships;
	}
	
	public void setUnitRoleMemberships(List<UnitRoleMembership> unitRoleMemberships){
		this.unitRoleMemberships = unitRoleMemberships;
	}
	
	@OneToMany(mappedBy = "unit", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<CredentialUnit> getCredentialUnits(){
		return credentialUnits;
	}
	
	public void setCredentialUnits(List<CredentialUnit> credentialUnits){
		this.credentialUnits = credentialUnits;
	}
	
	@OneToMany(mappedBy = "unit", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<CompetenceUnit> getCompetenceUnits(){
		return competenceUnits;
	}
	
	public void setCompetenceUnits(List<CompetenceUnit> competenceUnits){
		this.competenceUnits = competenceUnits;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public Unit getParentUnit(){
		return parentUnit;
	}
	
	public void setParentUnit(Unit parentUnit){
		this.parentUnit = parentUnit;
	}

	@OneToMany(mappedBy = "unit", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<RubricUnit> getRubricUnits(){
		return rubricUnits;
	}

	public void setRubricUnits(List<RubricUnit> rubricUnits){
		this.rubricUnits = rubricUnits;
	}

	@Column(length = 21844, columnDefinition="Text")
	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}
}
