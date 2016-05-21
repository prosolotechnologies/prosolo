package org.prosolo.search.util.credential;

import org.prosolo.web.search.data.SortingOption;

public class CredentialMembersSortOption {

	private CredentialMembersSortField sortField;
	private SortingOption sortOrder;
	
	public CredentialMembersSortOption() {
		
	}

	public CredentialMembersSortOption(CredentialMembersSortField sortField, SortingOption sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	public CredentialMembersSortField getSortField() {
		return sortField;
	}

	public void setSortField(CredentialMembersSortField sortField) {
		this.sortField = sortField;
	}

	public SortingOption getSortOption() {
		return sortOrder;
	}

	public void setSortOption(SortingOption sortOption) {
		this.sortOrder = sortOption;
	}
	
	
}
