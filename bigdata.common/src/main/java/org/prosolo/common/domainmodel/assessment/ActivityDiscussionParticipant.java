package org.prosolo.common.domainmodel.assessment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class ActivityDiscussionParticipant extends BaseEntity {

	private static final long serialVersionUID = 5477396985073390800L;
	

	private boolean read;
	private User participant;
	//private ActivityDiscussionMessage lastReadMessage;
	private ActivityAssessment activityDiscussion;
	
	@Type(type = "true_false")
	@Column(name="is_read", columnDefinition = "char(1) DEFAULT 'T'")
	public boolean isRead() {
		return read;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}
	
	@ManyToOne
	public User getParticipant() {
		return participant;
	}
	
	public void setParticipant(User participant) {
		this.participant = participant;
	}

//	@OneToOne
//	public ActivityDiscussionMessage getLastReadMessage() {
//		return lastReadMessage;
//	}
//
//	public void setLastReadMessage(ActivityDiscussionMessage lastReadMessage) {
//		this.lastReadMessage = lastReadMessage;
//	}

	@ManyToOne
	@JoinColumn(referencedColumnName="id")
	public ActivityAssessment getActivityDiscussion() {
		return activityDiscussion;
	}

	public void setActivityDiscussion(ActivityAssessment activityDiscussion) {
		this.activityDiscussion = activityDiscussion;
	}
	
	
	

}
