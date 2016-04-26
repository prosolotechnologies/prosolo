package org.prosolo.search.util.course;

import org.prosolo.web.search.data.SortingOption;

public class CourseMembersSortOption {

	private CourseMembersSortField sortField;
	private SortingOption sortOrder;
	
	public CourseMembersSortOption() {
		
	}

	public CourseMembersSortOption(CourseMembersSortField sortField, SortingOption sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	public CourseMembersSortField getSortField() {
		return sortField;
	}

	public void setSortField(CourseMembersSortField sortField) {
		this.sortField = sortField;
	}

	public SortingOption getSortOption() {
		return sortOrder;
	}

	public void setSortOption(SortingOption sortOption) {
		this.sortOrder = sortOption;
	}
	
	
}
