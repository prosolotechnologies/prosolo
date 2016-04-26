package org.prosolo.search.util.credential;

import org.prosolo.services.util.SortingOption;

public enum CredentialSortOption {

	NEWEST_FIRST("Newest first", "dateCreated", SortingOption.DESC),
	//add when implemented
	//RELEVANCE("Relevance", "", SortingOption.ASC),
	ALPHABETICALLY("Alphabetically", "title", SortingOption.ASC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String sortField;
	private SortingOption sortOrder;
	
	private CredentialSortOption(String label, String sortField, SortingOption sortOrder) {
		this.label = label;
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	public String getLabel() {
		return label;
	}

	public String getSortField() {
		return sortField;
	}

	public SortingOption getSortOrder() {
		return sortOrder;
	}
	
}