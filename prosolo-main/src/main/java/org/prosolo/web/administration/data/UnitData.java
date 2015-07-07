package org.prosolo.web.administration.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.organization.OrganizationalUnit;
import org.prosolo.common.domainmodel.organization.Unit_User;

public class UnitData implements Serializable {

	private static final long serialVersionUID = -3995523183331429992L;

	private String name;
	private String parentName;
	private String description;
	private List<UnitUserData> unitUsers;
	private List<UnitData> subUnits;
	private long id;
	private long parentId;
	private String parentUri;
	private OrganizationalUnit orgUnit;

	public UnitData() { }

	public UnitData(OrganizationalUnit unit) {
		this.name = unit.getTitle();

		OrganizationalUnit parentUnit = unit.getParentUnit();
		this.parentName = parentUnit != null ? parentUnit.getTitle() : "None";
		this.description = unit.getDescription();
		this.id = unit.getId();
		this.subUnits = new ArrayList<UnitData>();
		this.unitUsers = new ArrayList<UnitUserData>();
		this.orgUnit = unit;

		Set<Unit_User> usersOfTheUnit = unit.getUnitUser();

		Iterator<Unit_User> uuiterator = usersOfTheUnit.iterator();

		while (uuiterator.hasNext()) {
			Unit_User uu = uuiterator.next();
			UnitUserData unitUser = new UnitUserData(uu);
			unitUser.setActive(uu.isActive());
			this.unitUsers.add(unitUser);
		}

		Collection<OrganizationalUnit> subOrgUnits = unit.getSubUnits();
		
		if (subOrgUnits != null) {
			Iterator<OrganizationalUnit> iterator = subOrgUnits.iterator();

			while (iterator.hasNext()) {
				OrganizationalUnit subUnit = iterator.next();
				UnitData newSubUnit = new UnitData(subUnit);
				this.subUnits.add(newSubUnit);
			}
		}
	}

	public List<UnitUserData> getUnitUsers() {
		return unitUsers;
	}

	public void setUnitUsers(List<UnitUserData> unitUsers) {
		this.unitUsers = unitUsers;
	}

	public String getParentUri() {
		return parentUri;
	}

	public void setParentUri(String parentUri) {
		this.parentUri = parentUri;
	}
	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

	public List<UnitData> getSubUnits() {
		return subUnits;
	}

	public void setSubUnits(List<UnitData> subUnits) {
		this.subUnits = subUnits;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public OrganizationalUnit getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(OrganizationalUnit orgUnit) {
		this.orgUnit = orgUnit;
	}

	public void updateUnit(OrganizationalUnit unit) {
		if (unit != null) {
			unit.setTitle(this.getName());
			unit.setDescription(this.getDescription());
//			unit.setUri(this.getUri());
		}
	}



}
