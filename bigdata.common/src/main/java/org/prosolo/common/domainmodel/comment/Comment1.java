package org.prosolo.common.domainmodel.comment;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Comment1 extends BaseEntity {

	private static final long serialVersionUID = -620642224761542131L;

	private int likeCount;
	private Comment1 parentComment;
	private List<Comment1> childComments;
	
	private long commentedResourceId;
	private CommentedResourceType resourceType;
	
	private User user;
	private boolean isInstructor;
	
	//date from baseentity not saving hours, minutes
	private Date postDate;
	
	public Comment1() {
		
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	@ManyToOne(fetch = FetchType.LAZY)
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

	@ManyToOne(fetch = FetchType.LAZY)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isInstructor() {
		return isInstructor;
	}

	public void setInstructor(boolean isInstructor) {
		this.isInstructor = isInstructor;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	
}
