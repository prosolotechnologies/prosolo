package org.prosolo.search.util.competences;

public enum CompetenceSearchFilter {

	ACTIVE("Active"), 
	PUBLISHED("Published"),
	DRAFT("Drafts"),
	ARCHIVED("Archived");
	
	private String label;
	
	private CompetenceSearchFilter(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
}