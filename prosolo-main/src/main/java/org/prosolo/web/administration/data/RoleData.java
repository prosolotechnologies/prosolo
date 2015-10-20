package org.prosolo.web.administration.data;

import java.io.Serializable;
import java.util.List;

import org.prosolo.common.domainmodel.organization.Role;

public class RoleData implements Serializable {

	private static final long serialVersionUID = 2825050013257617040L;

	private String name;
	private String description;
	private long id;
	private boolean active;
	private List<Long> userIds;

	public RoleData() { }

	public RoleData(Role role) {
		this.name = role.getTitle();
		this.description = role.getDescription();
		this.id = role.getId();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
	
	public String toString() {
		return this.getName();
	}

	public List<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}
	
	
}
