package org.prosolo.search.util.credential;

public enum CredentialSearchFilterManager {

	ACTIVE("Active"),
	ARCHIVED("Archived");
	
	private String label;
	
	private CredentialSearchFilterManager(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}