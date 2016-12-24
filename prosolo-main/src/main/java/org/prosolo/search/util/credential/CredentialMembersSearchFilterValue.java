package org.prosolo.search.util.credential;

public enum CredentialMembersSearchFilterValue {
	All("All"), 
	Unassigned("Only unassigned"),
	Assigned("Only assigned"),
	Completed("Completed");
	
	private String label;
	
	private CredentialMembersSearchFilterValue(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
