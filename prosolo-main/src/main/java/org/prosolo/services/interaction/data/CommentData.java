package org.prosolo.services.interaction.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.prosolo.services.nodes.data.UserData;

public class CommentData {

	private long commentId;
	private long parentCommentId;
	private long commentedResourceId;
	private String comment;
	private UserData creator;
	private boolean isInstructor;
	private int likeCount;
	private List<UserData> likedBy;
	private boolean likedByCurrentUser;
	private List<CommentData> childComments;
	private Date dateCreated;
	
	public CommentData() {
		likedBy = new ArrayList<>();
		childComments = new ArrayList<>();
	}
	
	public long getCommentId() {
		return commentId;
	}
	public void setCommentId(long commentId) {
		this.commentId = commentId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}

	public boolean isInstructor() {
		return isInstructor;
	}
	public void setInstructor(boolean isInstructor) {
		this.isInstructor = isInstructor;
	}
	public int getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
	public List<UserData> getLikedBy() {
		return likedBy;
	}
	public void setLikedBy(List<UserData> likedBy) {
		this.likedBy = likedBy;
	}
	public List<CommentData> getChildComments() {
		return childComments;
	}
	public void setChildComments(List<CommentData> childComments) {
		this.childComments = childComments;
	}

	public boolean isLikedByCurrentUser() {
		return likedByCurrentUser;
	}

	public void setLikedByCurrentUser(boolean likedByCurrentUser) {
		this.likedByCurrentUser = likedByCurrentUser;
	}

	public long getParentCommentId() {
		return parentCommentId;
	}

	public void setParentCommentId(long parentCommentId) {
		this.parentCommentId = parentCommentId;
	}

	public long getCommentedResourceId() {
		return commentedResourceId;
	}

	public void setCommentedResourceId(long commentedResourceId) {
		this.commentedResourceId = commentedResourceId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
}
