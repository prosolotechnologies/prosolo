package org.prosolo.common.domainmodel.organization;

import javax.persistence.*;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author Bojan
 *
 * May 15, 2017
 */

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user", "unit", "role"})})
public class UnitRoleMembership extends BaseEntity {

	private static final long serialVersionUID = 5292392502819704285L;
	
	private User user;
	private Unit unit;
	private Role role;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Unit getUnit(){
		return unit;
	}
	
	public void setUnit(Unit unit){
		this.unit = unit;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Role getRole(){
		return role;
	}
	
	public void setRole(Role role){
		this.role = role;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getUser(){
		return user;
	}
	
	public void setUser(User user){
		this.user = user;
	}
}
