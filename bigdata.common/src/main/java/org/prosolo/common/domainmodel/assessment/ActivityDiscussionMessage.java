package org.prosolo.common.domainmodel.assessment;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class ActivityDiscussionMessage extends BaseEntity {

	private static final long serialVersionUID = -8443962698210679540L;
	
	private String content;
	private ActivityDiscussionParticipant sender;
	private ActivityDiscussion discussion;
	private Date lastUpdated;
	
	@Column(name = "content", nullable = true, length=9000)
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@OneToOne
	public ActivityDiscussionParticipant getSender() {
		return sender;
	}
	
	public void setSender(ActivityDiscussionParticipant sender) {
		this.sender = sender;
	}
	
	@ManyToOne
	@JoinColumn(referencedColumnName="id")
	public ActivityDiscussion getDiscussion() {
		return discussion;
	}
	
	
	public void setDiscussion(ActivityDiscussion discussion) {
		this.discussion = discussion;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", length = 19)
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	
}
