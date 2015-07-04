package org.prosolo.common.domainmodel.organization;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;

@Entity
//@Table(name="org_Position")
public class OrganizationalPosition extends BaseEntity {

	private static final long serialVersionUID = 7882101894240336169L;

	/**
	 * Establishes a relationship between 
	 * an organizational position and the organizational unit it was assigned (tied) to.
	 */
	private OrganizationalUnit allocatedToOrgUnit;
	
	/**
	 * The relationship between an organizational position and duties that are assigned to it.
	 */
//	private Collection<Duty> assignedDuties;
//	private Collection<CompetenceRequirement> requiredCompetences;
	
	public OrganizationalPosition() {
//		assignedDuties = new ArrayList<Duty>();
//		requiredCompetences = new ArrayList<CompetenceRequirement>();
	}

	@OneToOne
	public OrganizationalUnit getAllocatedToOrgUnit() {
		return allocatedToOrgUnit;
	}

	public void setAllocatedToOrgUnit(OrganizationalUnit allocatedToOrgUnit) {
		this.allocatedToOrgUnit = allocatedToOrgUnit;
	}

//	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}) 
//	@JoinColumn(name="org_OrgPosition_assignedDuties_Duty")
//	public Collection<Duty> getAssignedDuties() {
//		return assignedDuties;
//	}
//
//	public void setAssignedDuties(Collection<Duty> assignedDuties) {
//		this.assignedDuties = assignedDuties;
//	}
//	
//	public void addAssignedDuty(Duty duty) {
//		if (duty != null) {
//			if (!getAssignedDuties().contains(duty)) {
//				getAssignedDuties().add(duty);
//			}
//		} else
//			throw new RuntimeException("duty must not be null.");
//	}
//
//	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}) 
//	@JoinTable(name="org_OrgPosition_CompRequirement")
//	public Collection<CompetenceRequirement> getRequiredCompetences() {
//		return requiredCompetences;
//	}
//	
//	public void setRequiredCompetences(Collection<CompetenceRequirement> requiresCompetences) {
//		this.requiredCompetences = requiresCompetences;
//	}
//	
//	public void addRequiredCompetence(CompetenceRequirement reqComp) {
//		if ( reqComp != null ) {
//			if ( !getRequiredCompetences().contains(reqComp) ) {
//				getRequiredCompetences().add(reqComp);
//			}
//		}
//	}

}
