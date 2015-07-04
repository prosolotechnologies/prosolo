package org.prosolo.common.domainmodel.organization;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.organization.OrganizationalUnit;

@Entity
//@Table(name="org_Unit_User")
public class Unit_User extends BaseEntity {

	private static final long serialVersionUID = 2354886200208826244L;

	private OrganizationalUnit unit;
	private User user;
//	private Set<Unit_User_Role> unitUserRole;
	
	private String position;

	private boolean active;
	private Date activationDate;
	private Date deactivationDate;
	
	public Unit_User() {
//		unitUserRole = new HashSet<Unit_User_Role>();
	}
	
	public Unit_User(OrganizationalUnit unit, User user, String position, Date activationDate) {
		this();
		
		this.unit = unit;
		this.user = user;
		this.position = position;
		this.active = true;
		this.activationDate = activationDate;
	}

	@OneToOne
	public OrganizationalUnit getUnit() {
		return unit;
	}

	public void setUnit(OrganizationalUnit unit) {
		this.unit = unit;
	}

	@OneToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

//	@OneToMany(fetch=FetchType.LAZY)
//	@JoinTable(name="org_UnitUser_UnitUserRole")
//	public Set<Unit_User_Role> getUnitUserRole() {
//		return unitUserRole;
//	}
//
//	public void setUnitUserRole(Set<Unit_User_Role> unitUserRole) {
//		this.unitUserRole = unitUserRole;
//	}
//	
//	public void addUnitUserRole(Unit_User_Role unitUserRole) {
//		if (unitUserRole != null) {
//			if (!getUnitUserRole().contains(unitUserRole)) {
//				getUnitUserRole().add(unitUserRole);
//			}
//		}
//	}
	
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
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
