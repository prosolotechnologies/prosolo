package org.prosolo.common.domainmodel.user;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.general.BaseEntity;

public class UserGroup extends BaseEntity {

	private static final long serialVersionUID = 5056103791488544103L;
	
	private List<UserGroupUser> users;
	private UserGroupPrivilege privilege = UserGroupPrivilege.View;
	private String name;
	//group where users are put by default, it is not created manually and it is not visible to end users
	private boolean defaultGroup;
	
	@OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, orphanRemoval = true)
	public List<UserGroupUser> getUsers() {
		return users;
	}

	public void setUsers(List<UserGroupUser> users) {
		this.users = users;
	}

	@Enumerated(EnumType.STRING)
	public UserGroupPrivilege getPrivilege() {
		return privilege;
	}
	
	public void setPrivilege(UserGroupPrivilege privilege) {
		this.privilege = privilege;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDefaultGroup() {
		return defaultGroup;
	}
	
	public void setDefaultGroup(boolean defaultGroup) {
		this.defaultGroup = defaultGroup;
	}
	
}
