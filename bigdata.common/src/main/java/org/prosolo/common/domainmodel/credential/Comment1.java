package org.prosolo.common.domainmodel.credential;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class Comment1 extends BaseEntity {

	private static final long serialVersionUID = -620642224761542131L;

	private int likeCount;
	private Comment1 parentComment;
	private List<Comment1> childComments;
	
	private long commentedResourceId;
	private CommentedResourceType resourceType;
	
	//user reference or denormalization
	private long userId;
	private String name;
	private String userAvatar;
	private boolean isInstructor;
	
	public Comment1() {
		
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Comment1 getParentComment() {
		return parentComment;
	}

	public void setParentComment(Comment1 parentComment) {
		this.parentComment = parentComment;
	}

	@OneToMany(mappedBy = "parentComment")
	public List<Comment1> getChildComments() {
		return childComments;
	}

	public void setChildComments(List<Comment1> childComments) {
		this.childComments = childComments;
	}

	public long getCommentedResourceId() {
		return commentedResourceId;
	}

	public void setCommentedResourceId(long commentedResourceId) {
		this.commentedResourceId = commentedResourceId;
	}

	@Enumerated(EnumType.STRING)
	public CommentedResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(CommentedResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserAvatar() {
		return userAvatar;
	}

	public void setUserAvatar(String userAvatar) {
		this.userAvatar = userAvatar;
	}

	public boolean isInstructor() {
		return isInstructor;
	}

	public void setInstructor(boolean isInstructor) {
		this.isInstructor = isInstructor;
	}
	
}
