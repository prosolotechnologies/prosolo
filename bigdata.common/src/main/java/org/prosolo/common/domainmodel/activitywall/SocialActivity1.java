package org.prosolo.common.domainmodel.activitywall;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(length=100)
public class SocialActivity1 extends BaseEntity {

	private static final long serialVersionUID = 7765878968041421739L;
	
	private String text;
	private boolean commentsDisabled;
	private List<Comment1> comments;
	private Date lastAction;
	
	private int likeCount;
	
	private User actor;
	
	public SocialActivity1() {
		comments = new ArrayList<Comment1>();
	}
	
	@Column(length = 90000)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isCommentsDisabled() {
		return commentsDisabled;
	}

	public void setCommentsDisabled(boolean commentsDisabled) {
		this.commentsDisabled = commentsDisabled;
	}

	@OneToMany
	public List<Comment1> getComments() {
		return comments;
	}

	public void setComments(List<Comment1> comments) {
		this.comments = comments;
	}
	
	public boolean addComment(Comment1 comment) {
		if (comment != null) {
			return getComments().add(comment);
		}
		return false;
	}
	
	public Date getLastAction() {
		return lastAction;
	}

	public void setLastAction(Date lastAction) {
		this.lastAction = lastAction;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getActor() {
		return actor;
	}

	public void setActor(User actor) {
		this.actor = actor;
	}

	@Override
	public String toString() {
		return "SocialActivity [text=" + text
				+ ", commentsDisabled="
				+ commentsDisabled + ", comments=" + comments + ", lastAction="
				+ lastAction + ", likeCount="
				+ likeCount + "]";
	}
	
}
