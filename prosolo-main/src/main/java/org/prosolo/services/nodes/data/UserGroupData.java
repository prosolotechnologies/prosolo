package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.List;

public class UserGroupData implements Serializable {

	private static final long serialVersionUID = -3541954295853729418L;
	
	private long id;
	private String name;
	private long userCount;
	private List<UserData> users;
	
	public UserGroupData() {
		
	}
	
	public UserGroupData(long id, String name, long userCount) {
		this.id = id;
		this.name = name;
		this.userCount = userCount;
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
	
}
