package org.prosolo.domainmodel.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.MessagesThread;
import org.prosolo.domainmodel.user.User;

@Entity
//@Table(name = "user_SimpleOfflineMessage")
public class SimpleOfflineMessage extends BaseEntity {

	private static final long serialVersionUID = -2686037343840070507L;

	private User receiver;
	private User sender; 
	private String content;
	private boolean read;
	private MessagesThread messageThread;

	// private String subject;

	@ManyToOne
	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	@ManyToOne
	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	@Column(name = "content", nullable = true, length=9000)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
 
	@Type(type = "true_false")
	@Column(name = "readed", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	@ManyToOne
	@JoinColumn(name="messageThread_id")
	public MessagesThread getMessageThread() {
		return messageThread;
	}

	public void setMessageThread(MessagesThread messageThread) {
		this.messageThread = messageThread;
	}

}
