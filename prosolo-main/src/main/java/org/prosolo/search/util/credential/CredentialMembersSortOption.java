package org.prosolo.search.util.credential;

import org.prosolo.services.util.SortingOption;

public enum CredentialMembersSortOption {

	STUDENT_NAME("Alphabetically", new String[] {"name", "lastname"}, SortingOption.ASC),  
	DATE("Recent", new String[] {"credentials.dateEnrolled"}, SortingOption.DESC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String[] sortFields;
	private SortingOption sortOrder;
	
	private CredentialMembersSortOption(String label, String[] sortFields, SortingOption sortOrder) {
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
