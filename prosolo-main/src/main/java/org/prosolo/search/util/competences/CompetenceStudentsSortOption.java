package org.prosolo.search.util.competences;

import org.prosolo.services.util.SortingOption;

public enum CompetenceStudentsSortOption {

	STUDENT_NAME("Alphabetically", new String[] {"lastname.sort", "name.sort"}, false, SortingOption.ASC),
	DATE("Recent", new String[] {"competences.dateEnrolled"}, true, SortingOption.DESC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String[] sortFields;
	private boolean nestedSort;
	private SortingOption sortOrder;
	
	CompetenceStudentsSortOption(String label, String[] sortFields, boolean nestedSort, SortingOption sortOrder) {
		this.label = label;
		this.sortFields = sortFields;
		this.nestedSort = nestedSort;
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

	public boolean isNestedSort() {
		return nestedSort;
	}
}
