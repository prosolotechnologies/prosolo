package org.prosolo.common.domainmodel.messaging;


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
public class Message extends BaseEntity {

	private static final long serialVersionUID = -2686037343840070507L;

	private ThreadParticipant sender; 
	private String content;
	private MessageThread messageThread;
	private Date createdTimestamp;

	// private String subject;

	@OneToOne
	public ThreadParticipant getSender() {
		return sender;
	}

	public void setSender(ThreadParticipant sender) {
		this.sender = sender;
	}

	@Column(name = "content", nullable = true, length=9000)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
 
	@ManyToOne
	@JoinColumn(referencedColumnName="id")
	public MessageThread getMessageThread() {
		return messageThread;
	}

	public void setMessageThread(MessageThread messageThread) {
		this.messageThread = messageThread;
	}

	@Column(name = "created_timestamp", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	
}
