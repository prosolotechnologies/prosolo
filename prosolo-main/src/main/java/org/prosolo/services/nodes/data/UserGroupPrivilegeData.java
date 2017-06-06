package org.prosolo.services.nodes.data;

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
