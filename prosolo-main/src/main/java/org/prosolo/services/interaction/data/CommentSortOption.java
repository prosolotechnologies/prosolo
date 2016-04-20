package org.prosolo.services.interaction.data;

import org.prosolo.services.util.SortingOption;

public enum CommentSortOption {

	NEWEST_FIRST("Newest first", CommentSortField.DATE_CREATED, SortingOption.DESC),
	OLDEST_FIRST("Oldest first", CommentSortField.DATE_CREATED, SortingOption.ASC),
	MOST_UPVOTED("Most upvoted", CommentSortField.LIKE_COUNT, SortingOption.DESC);
	
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
