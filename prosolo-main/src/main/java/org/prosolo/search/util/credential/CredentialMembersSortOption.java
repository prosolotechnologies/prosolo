package org.prosolo.search.util.credential;

import org.prosolo.services.util.SortingOption;

public enum CredentialMembersSortOption {

	STUDENT_NAME("Alphabetically", new String[] {"lastname.sort", "name.sort"}, false, SortingOption.ASC),
	DATE("Recent", new String[] {"credentials.dateEnrolled"}, true, SortingOption.DESC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String[] sortFields;
	private SortingOption sortOrder;
	private boolean nestedSort;
	
	CredentialMembersSortOption(String label, String[] sortFields, boolean nestedSort, SortingOption sortOrder) {
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
