package org.prosolo.search.util.credential;

public enum CredentialSearchFilter {

	ALL("All"), 
	BOOKMARKS("Bookmarks"),
	UNIVERSITY("University"),
	FROM_CREATOR("Created by you"),
	FROM_OTHER_STUDENTS("From other students"),
	FROM_STUDENTS("By students"),
	YOUR_CREDENTIALS("Your credentials");
	
	private String label;
	
	private CredentialSearchFilter(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}