package org.prosolo.search.util.credential;

public enum CredentialSearchFilterUser {

	ALL("All"), 
	ENROLLED("Enrolled"),
	BOOKMARKS("Bookmarks");
	
	private String label;
	
	private CredentialSearchFilterUser(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}