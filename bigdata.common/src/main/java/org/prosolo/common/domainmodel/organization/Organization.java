package org.prosolo.common.domainmodel.organization;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;

@Entity
//@Table(name = "org_Organization")
//@Indexed
public class Organization extends BaseEntity {

	private static final long serialVersionUID = -8154835729383895084L;

	private String name;
	private String abbreviatedName;
	private Collection<OrganizationalUnit> orgUnits;
	
	public Organization() {
		orgUnits = new ArrayList<OrganizationalUnit>();
	}

	/**
	 * @return the name
	 */
	@Column(name = "name", nullable = true)
	//@Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getAbbreviatedName() {
		return abbreviatedName;
	}
	
	public void setAbbreviatedName(String abbreviatedName) {
		this.abbreviatedName = abbreviatedName;
	}
	
	@OneToMany
	@Cascade({CascadeType.MERGE})
	@JoinTable(name = "org_Organization_orgUnits_OrganizationalUnit")
	public Collection<OrganizationalUnit> getOrgUnits() {
		return orgUnits;
	}

	public void setOrgUnits(Collection<OrganizationalUnit> orgUnits) {
		this.orgUnits = orgUnits;
	}

	public void addOrgUnit(OrganizationalUnit orgUnit) {
		if (orgUnit != null) {
			if (!getOrgUnits().contains(orgUnit)) {
				getOrgUnits().add(orgUnit);
			}
		}
	}

}
