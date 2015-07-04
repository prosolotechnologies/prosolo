package org.prosolo.domainmodel.organization;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.organization.Role;
import org.prosolo.domainmodel.organization.Unit_User;

@Entity
//@Table(name="org_Unit_User_Role")
public class Unit_User_Role extends BaseEntity {

	private static final long serialVersionUID = -5022382688056897911L;

	private Role role;
	private Unit_User unitUser;

	private boolean active;
	private Date activationDate;
	private Date deactivationDate;
	
	public Unit_User_Role() { }
	
	public Unit_User_Role(Role role, Unit_User unitUser, Date activationDate) {
		this.role = role;
		this.unitUser = unitUser;
		
		this.active = true;
		this.activationDate = activationDate;
	}

	@OneToOne
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@OneToOne
	public Unit_User getUnitUser() {
		return unitUser;
	}

	public void setUnitUser(Unit_User unitUser) {
		this.unitUser = unitUser;
	}

	@Column(name="active", nullable=true)
	@Type(type="true_false")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "activation_date", length = 19)
	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "deactivation_date", length = 19)
	public Date getDeactivationDate() {
		return deactivationDate;
	}

	public void setDeactivationDate(Date deactivationDate) {
		this.deactivationDate = deactivationDate;
	}

}
