package org.prosolo.domainmodel.organization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.organization.Organization;
import org.prosolo.domainmodel.organization.OrganizationalUnit;
import org.prosolo.domainmodel.organization.Unit_User;

@Entity
//@Table(name="org_OrgUnit")
public class OrganizationalUnit extends BaseEntity {

	private static final long serialVersionUID = -2933552713275195785L;

	private Organization organization;
	private OrganizationalUnit parentUnit;
	private Collection<OrganizationalUnit> subUnits;
	private Set<Unit_User> unitUser;
	
	private boolean system;
	
	public OrganizationalUnit() {
		this.subUnits = new ArrayList<OrganizationalUnit>();
		this.unitUser = new HashSet<Unit_User>();
		this.system = false;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(Organization organization) {
		if (null != organization) {
			this.organization = organization;
		}  
	}

	@OneToOne(fetch=FetchType.LAZY)
	public OrganizationalUnit getParentUnit() {
		return parentUnit;
	}

	public void setParentUnit(OrganizationalUnit parentUnit) {
		this.parentUnit = parentUnit;
	}

	@OneToMany
	@Cascade({CascadeType.MERGE})
	@JoinColumn(name="org_OrganizationalUnit_subUnits_OrganizationalUnit")
	public Collection<OrganizationalUnit> getSubUnits() {
		return subUnits;
	}

	public void setSubUnits(Collection<OrganizationalUnit> subUnits) {
		this.subUnits = subUnits;
	}

	public void addSubUnit(OrganizationalUnit orgUnit) {
		if ( orgUnit != null ) {
			if ( !getSubUnits().contains(orgUnit) )
				getSubUnits().add(orgUnit);
		}
	}

	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="org_Unit_UnitUser")
	public Set<Unit_User> getUnitUser() {
		return unitUser;
	}

	public void setUnitUser(Set<Unit_User> unitUser) {
		this.unitUser = unitUser;
	}
	
	public void addUnitUser(Unit_User unitUser) {
		if (unitUser != null) {
			if (!getUnitUser().contains(unitUser)) {
				getUnitUser().add(unitUser);
			}
		}
	}

	@Type(type="true_false")
	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}
	
}
