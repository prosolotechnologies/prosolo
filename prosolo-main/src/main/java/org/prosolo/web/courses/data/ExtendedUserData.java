package org.prosolo.web.courses.data;

public class ExtendedUserData {

	private BasicUserData user;
	private boolean isNew;
	
	public ExtendedUserData() {
		
	}
	
	public ExtendedUserData(BasicUserData user, boolean isNew) {
		this.user = user;
		this.isNew = isNew;
	}

	public BasicUserData getUser() {
		return user;
	}

	public void setUser(BasicUserData user) {
		this.user = user;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
}
