package org.prosolo.services.interaction.data;

public enum CommentSortField {

	DATE_CREATED("postDate"),
	LIKE_COUNT("likeCount");
	
	
    private String sortField; 

    
    CommentSortField(String sortField) {
        this.sortField = sortField;
    }

	public String getSortField() {
		return sortField;
	}

}
