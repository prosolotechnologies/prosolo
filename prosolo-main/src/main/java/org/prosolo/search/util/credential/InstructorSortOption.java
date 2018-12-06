package org.prosolo.search.util.credential;

import org.prosolo.services.util.SortingOption;

public enum InstructorSortOption {

	Instructor_Name("Alphabetically", new String[] {"lastname.sort", "name.sort"}, SortingOption.ASC),
	Date("Recent", new String[] {"credentialsWithInstructorRole.dateAssigned"}, SortingOption.DESC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String[] sortFields;
	private SortingOption sortOrder;
	
	private InstructorSortOption(String label, String[] sortFields, SortingOption sortOrder) {
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
