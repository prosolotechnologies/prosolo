package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.List;

import org.prosolo.common.domainmodel.user.UserGroup;

public class UserGroupData implements Serializable {

	private static final long serialVersionUID = -3541954295853729418L;
	
	private long id;
	private String name;
	private long userCount;
	private List<UserData> users;
	private boolean isUserInGroup;
	private boolean joinUrlActive;
	private String joinUrlPassword;
	private boolean joinUrlPasswordRequired;
	private boolean canBeDeleted;
	private long unitId;
	
	public UserGroupData() {
		
	}
	
	public UserGroupData(long id, String name, boolean canBeDeleted, long userCount) {
		this.id = id;
		this.name = name;
		this.userCount = userCount;
		this.canBeDeleted = canBeDeleted;
	}
	
	public UserGroupData(long id, String name, long userCount, boolean isUserInGroup) {
		this.id = id;
		this.name = name;
		this.userCount = userCount;
		this.isUserInGroup = isUserInGroup;
	}
	
	public UserGroupData(UserGroup group) {
		this.id = group.getId();
		this.name = group.getName();
		this.joinUrlActive = group.isJoinUrlActive();
		this.joinUrlPassword = group.getJoinUrlPassword();
		this.joinUrlPasswordRequired = joinUrlPassword != null && joinUrlPassword.length() > 0;
		this.unitId = group.getUnit().getId();
	}

	public UserGroupData(long userCount, boolean canBeDeleted) {
		this.userCount = userCount;
		this.canBeDeleted = canBeDeleted;
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
	public List<UserData> getUsers() {
		return users;
	}
	public void setUsers(List<UserData> users) {
		this.users = users;
	}

	public long getUserCount() {
		return userCount;
	}

	public void setUserCount(long userCount) {
		this.userCount = userCount;
	}

	public boolean isUserInGroup() {
		return isUserInGroup;
	}

	public void setUserInGroup(boolean isUserInGroup) {
		this.isUserInGroup = isUserInGroup;
	}

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

	public boolean isCanBeDeleted() {
		return canBeDeleted;
	}

	public long getUnitId() {
		return unitId;
	}

	public boolean isJoinUrlPasswordRequired() {
		return joinUrlPasswordRequired;
	}

	public void setJoinUrlPasswordRequired(boolean joinUrlPasswordRequired) {
		this.joinUrlPasswordRequired = joinUrlPasswordRequired;
	}
}
