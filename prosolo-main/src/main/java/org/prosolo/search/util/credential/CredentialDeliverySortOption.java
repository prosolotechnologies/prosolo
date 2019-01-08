package org.prosolo.search.util.credential;

import org.prosolo.services.common.data.SortingOption;

public enum CredentialDeliverySortOption {

	DELIVERY_ORDER(SortingOption.ASC),
	DATE_STARTED(SortingOption.DESC),
	//add when implemented
	//RELEVANCE("Relevance", "", SortingOption.ASC),
	ALPHABETICALLY(SortingOption.ASC);

	private SortingOption sortOrder;

	CredentialDeliverySortOption(SortingOption sortOrder) {
		this.sortOrder = sortOrder;
	}

	public SortingOption getSortOrder() {
		return sortOrder;
	}

}
