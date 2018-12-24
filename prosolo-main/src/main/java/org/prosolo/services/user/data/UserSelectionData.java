package org.prosolo.services.user.data;

import java.io.Serializable;

import org.prosolo.common.domainmodel.user.User;

public class UserSelectionData implements Serializable {
	
	private static final long serialVersionUID = 1804381706626451163L;
	
	private UserData user;
	private boolean selected;
	
	public UserSelectionData() {}
	
	public UserSelectionData(User user) {
		this.user = new UserData(user);
	}
	
	public UserSelectionData(UserData user, boolean selected) {
		this.user = user;
		this.selected = selected;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
