package org.prosolo.common.domainmodel.activitywall;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.comment.Comment1;

@Entity
public class CommentSocialActivity extends SocialActivity1 {

	private static final long serialVersionUID = -3577215858161072801L;
	
	private Comment1 commentObject;

	@ManyToOne(fetch = FetchType.LAZY)
	public Comment1 getCommentObject() {
		return commentObject;
	}

	public void setCommentObject(Comment1 commentObject) {
		this.commentObject = commentObject;
	}

}
