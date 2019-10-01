package org.prosolo.common.domainmodel.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.Unit;

@Entity
public class UserGroup extends BaseEntity {

	private static final long serialVersionUID = 5056103791488544103L;
	
	private List<UserGroupUser> users;
	private List<UserGroupInstructor> instructors;
	private String name;
	private boolean joinUrlActive;
	private String joinUrlPassword;
	//group where users are put by default, it is not created manually and it is not visible to end users
	private boolean defaultGroup;
	//user group belongs to a unit
	private Unit unit;
	private List<CredentialUserGroup> credentialUserGroups;
	private List<CompetenceUserGroup> competenceUserGroups;
	
	public UserGroup() {
		users = new ArrayList<>();
	}
	
	@OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<UserGroupUser> getUsers() {
		return users;
	}

	public void setUsers(List<UserGroupUser> users) {
		this.users = users;
	}

	@OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<UserGroupInstructor> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<UserGroupInstructor> instructors) {
		this.instructors = instructors;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isJoinUrlActive() {
		return joinUrlActive;
	}

	public void setJoinUrlActive(boolean joinUrlActive) {
		this.joinUrlActive = joinUrlActive;
	}
	
	public String getJoinUrlPassword() {
		return joinUrlPassword;
	}

	public void setJoinUrlPassword(String joinUrlPassword) {
		this.joinUrlPassword = joinUrlPassword;
	}

	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDefaultGroup() {
		return defaultGroup;
	}
	
	public void setDefaultGroup(boolean defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	//unit should not be nullable column but there are existing groups for which we need to set null unit temporarily
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@OneToMany(mappedBy = "userGroup")
	public List<CredentialUserGroup> getCredentialUserGroups() {
		return credentialUserGroups;
	}

	public void setCredentialUserGroups(List<CredentialUserGroup> credentialUserGroups) {
		this.credentialUserGroups = credentialUserGroups;
	}

	@OneToMany(mappedBy = "userGroup")
	public List<CompetenceUserGroup> getCompetenceUserGroups() {
		return competenceUserGroups;
	}

	public void setCompetenceUserGroups(List<CompetenceUserGroup> competenceUserGroups) {
		this.competenceUserGroups = competenceUserGroups;
	}
}
