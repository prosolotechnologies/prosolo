package org.prosolo.search.util.credential;

import org.prosolo.services.util.SortingOption;

public enum LearningResourceSortOption {

	NEWEST_FIRST("Newest first", "dateCreated", "dateCreated", SortingOption.DESC),
	//add when implemented
	//RELEVANCE("Relevance", "", SortingOption.ASC),
	ALPHABETICALLY("Alphabetically", "title.sort", "title", SortingOption.ASC);
	
	//ui label
	private String label;
	//fields in elasticsearch corresponding to sort option
	private String sortField;
	//field in mysql db corresponding to sort option
	private String sortFieldDB;
	private SortingOption sortOrder;
	
	private LearningResourceSortOption(String label, String sortField, String sortFieldDB, SortingOption sortOrder) {
		this.label = label;
		this.sortField = sortField;
		this.sortFieldDB = sortFieldDB;
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

	public String getSortFieldDB() {
		return sortFieldDB;
	}
	
}
