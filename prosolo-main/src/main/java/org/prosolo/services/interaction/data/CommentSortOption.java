package org.prosolo.services.interaction.data;

import org.prosolo.services.common.data.SortingOption;

public enum CommentSortOption {

	MOST_RECENT("Most recent", CommentSortField.DATE_CREATED, SortingOption.DESC),
	MOST_UPVOTED("Most liked", CommentSortField.LIKE_COUNT, SortingOption.DESC);
	
	private String label;
	private CommentSortField sortField;
	private SortingOption sortOption;
	
	private CommentSortOption(String label, CommentSortField sortField, SortingOption sortOption) {
		this.label = label;
		this.sortField = sortField;
		this.sortOption = sortOption;
	}

	public String getLabel() {
		return label;
	}

	public CommentSortField getSortField() {
		return sortField;
	}

	public SortingOption getSortOption() {
		return sortOption;
	}
	
}
