package org.prosolo.search.util.credential;

public enum LearningResourceSearchFilter {

	ALL("All"), 
	ENROLLED("Enrolled"),
	BOOKMARKS("Bookmarks"),
	UNIVERSITY("University"),
	FROM_CREATOR("Created by me"),
	BY_OTHER_STUDENTS("By other students"),
	BY_STUDENTS("By students"),
	YOUR_CREDENTIALS("My credentials");
	
	private String label;
	
	private LearningResourceSearchFilter(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}