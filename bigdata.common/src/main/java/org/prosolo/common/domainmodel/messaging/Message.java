package org.prosolo.common.domainmodel.messaging;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class Message extends BaseEntity {

	private static final long serialVersionUID = -2686037343840070507L;

	private MessageParticipant sender; 
	private Set<MessageParticipant> participants;
	private String content;
	private MessageThread messageThread;

	// private String subject;

	@OneToOne
	public MessageParticipant getSender() {
		return sender;
	}

	public void setSender(MessageParticipant sender) {
		this.sender = sender;
	}

	@OneToMany
	@Cascade (CascadeType.ALL)
	public Set<MessageParticipant> getParticipants() {
		return participants;
	}

	public void setParticipants(Set<MessageParticipant> participants) {
		this.participants = participants;
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
