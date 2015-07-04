package org.prosolo.domainmodel.organization;

import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;

@Entity
//@Table(name = "org_Role")
public class Role extends BaseEntity {

	private static final long serialVersionUID = 267206036815318915L;
	
	private boolean system;
//	private Set<Unit_User_Role> unitUserRole;
	
	public Role() {
//		unitUserRole = new HashSet<Unit_User_Role>();
	}
	
	@Type(type="true_false")
	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

//	@OneToMany(fetch=FetchType.LAZY)
//	public Set<Unit_User_Role> getUnitUserRole() {
//		return unitUserRole;
//	}
//
//	public void setUnitUserRole(Set<Unit_User_Role> unitUserRole) {
//		this.unitUserRole = unitUserRole;
//	}
	
}
