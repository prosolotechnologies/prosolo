package org.prosolo.search.util.competences;

import org.prosolo.services.util.SortingOption;

public enum CompetenceStudentsSortOption {

	STUDENT_NAME("Alphabetically", new String[] {"lastname", "name"}, SortingOption.ASC),  
	DATE("Recent", new String[] {"competences.dateEnrolled"}, SortingOption.DESC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String[] sortFields;
	private SortingOption sortOrder;
	
	private CompetenceStudentsSortOption(String label, String[] sortFields, SortingOption sortOrder) {
		this.label = label;
		this.sortFields = sortFields;
		this.sortOrder = sortOrder;
	}

	public String getLabel() {
		return label;
	}

	public String[] getSortFields() {
		return sortFields;
	}

	public SortingOption getSortOrder() {
		return sortOrder;
	}

	
}
