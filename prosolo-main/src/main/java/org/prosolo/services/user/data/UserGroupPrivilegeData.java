package org.prosolo.services.user.data;

public enum UserGroupPrivilegeData {

	Learn("Can View"),
	Edit("Can Edit");
	
	private String label;
	
	private UserGroupPrivilegeData(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}
}
