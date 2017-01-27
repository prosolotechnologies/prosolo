package org.prosolo.search.util.credential;

public enum CredentialSearchFilter {

	ALL("All"), 
	BOOKMARKS("Bookmarks"),
	UNIVERSITY("University"),
	FROM_CREATOR("Created by you"),
	BY_OTHER_STUDENTS("By other students"),
	BY_STUDENTS("By students"),
	YOUR_CREDENTIALS("Your credentials"),
	ENROLLED("Enrolled");
	
	private String label;
	
	private CredentialSearchFilter(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}