package org.prosolo.search.util.credential;

import org.prosolo.search.data.SortingOption;

@Deprecated
public class CredentialMembersSortOption1 {

	private CredentialMembersSortOption sortField;
	private SortingOption sortOrder;
	
	public CredentialMembersSortOption1() {
		
	}

	public CredentialMembersSortOption1(CredentialMembersSortOption sortField, SortingOption sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	public CredentialMembersSortOption getSortField() {
		return sortField;
	}

	public void setSortField(CredentialMembersSortOption sortField) {
		this.sortField = sortField;
	}

	public SortingOption getSortOption() {
		return sortOrder;
	}

	public void setSortOption(SortingOption sortOption) {
		this.sortOrder = sortOption;
	}
	
	
}
