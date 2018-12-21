package org.prosolo.services.interaction.data;

import java.util.Date;

import org.prosolo.services.common.data.SortingOption;

public class CommentSortData {

	private CommentSortField sortField;
	private SortingOption sortOption;
	private Date previousDate;
	private int previousLikeCount;
	private long previousId;
	
	public CommentSortData(CommentSortField sortField, SortingOption sortOption, Date previousDate, 
			int previousLikeCount, long previousId) {
		this.sortField = sortField;
		this.sortOption = sortOption;
		this.previousDate = previousDate;
		this.previousLikeCount = previousLikeCount;
		this.previousId = previousId;
	}
	
	public CommentSortField getSortField() {
		return sortField;
	}
	public void setSortField(CommentSortField sortField) {
		this.sortField = sortField;
	}
	public Date getPreviousDate() {
		return previousDate;
	}
	public void setPreviousDate(Date previousDate) {
		this.previousDate = previousDate;
	}
	public int getPreviousLikeCount() {
		return previousLikeCount;
	}
	public void setPreviousLikeCount(int previousLikeCount) {
		this.previousLikeCount = previousLikeCount;
	}

	public SortingOption getSortOption() {
		return sortOption;
	}

	public void setSortOption(SortingOption sortOption) {
		this.sortOption = sortOption;
	}

	public long getPreviousId() {
		return previousId;
	}

	public void setPreviousId(long previousId) {
		this.previousId = previousId;
	}
	
}
