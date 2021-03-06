package org.prosolo.common.domainmodel.comment;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
//index added from the script
public class Comment1 extends BaseEntity {

	private static final long serialVersionUID = -620642224761542131L;

	private int likeCount;
	private Comment1 parentComment;
	private List<Comment1> childComments;
	
	private long commentedResourceId;
	private CommentedResourceType resourceType;
	
	private User user;
	private boolean isInstructor;
	//true if comment is posted from manage section
	private boolean isManagerComment;
	
	//date from baseentity not saving hours, minutes
	private Date postDate;

	/*
	credential this comment is related to; it applies to Competency and Activity comment so it should have a value for these comment types.
	In case of ActivityResult and SocialActivity comment, credential should be null.
	 */
	private Credential1 credential;
	
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

	@Column(name = "commented_resource_id")
	public long getCommentedResourceId() {
		return commentedResourceId;
	}

	public void setCommentedResourceId(long commentedResourceId) {
		this.commentedResourceId = commentedResourceId;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "resource_type", nullable = false)
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

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isManagerComment() {
		return isManagerComment;
	}

	public void setManagerComment(boolean isManagerComment) {
		this.isManagerComment = isManagerComment;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public Credential1 getCredential() {
		return credential;
	}

	public void setCredential(Credential1 credential) {
		this.credential = credential;
	}
}
