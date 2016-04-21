package org.prosolo.common.domainmodel.messaging;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class Message extends BaseEntity {

	private static final long serialVersionUID = -2686037343840070507L;

	private ThreadParticipant sender; 
	private String content;
	private MessageThread messageThread;

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
	@JoinColumn(name="messageThread_id")
	public MessageThread getMessageThread() {
		return messageThread;
	}

	public void setMessageThread(MessageThread messageThread) {
		this.messageThread = messageThread;
	}

}
