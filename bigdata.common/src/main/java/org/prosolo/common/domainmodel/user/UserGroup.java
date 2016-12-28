package org.prosolo.common.domainmodel.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class UserGroup extends BaseEntity {

	private static final long serialVersionUID = 5056103791488544103L;
	
	private List<UserGroupUser> users;
	private String name;
	//group where users are put by default, it is not created manually and it is not visible to end users
	private boolean defaultGroup;
	
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
