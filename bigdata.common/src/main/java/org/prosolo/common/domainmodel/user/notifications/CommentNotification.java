package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.common.domainmodel.activitywall.old.comments.Comment;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Entity
@DiscriminatorValue("CommentNotification")
@Deprecated
public class CommentNotification extends Notification {
	
	private static final long serialVersionUID = 7450319370700629949L;

	private Comment comment;
	
	@OneToOne
	public Comment getComment() {
		return comment;
	}
	
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
	@Transient
	public Comment getObject() {
		return this.comment;
	}
}
