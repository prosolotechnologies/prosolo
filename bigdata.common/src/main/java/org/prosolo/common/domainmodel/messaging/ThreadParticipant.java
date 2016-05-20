package org.prosolo.common.domainmodel.messaging;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class ThreadParticipant implements Serializable {

	private static final long serialVersionUID = 1156059969948940348L;
	
	private long id;
	private boolean read;
	private boolean archived;
	private User user;
	private Message lastReadMessage;
	private MessageThread messageThread;
	private boolean deleted;
	private Date showMessagesFrom;
	
	@Id
	@Column(name = "id",nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	@Type(type = "true_false")
	@Column(name="is_read", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	
	@Type(type = "true_false")
	@Column(name="archived", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@ManyToOne
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	@OneToOne
	public Message getLastReadMessage() {
		return lastReadMessage;
	}

	public void setLastReadMessage(Message lastReadMessage) {
		this.lastReadMessage = lastReadMessage;
	}
	
	@ManyToOne
	@JoinColumn(referencedColumnName="id")
	public MessageThread getMessageThread() {
		return messageThread;
	}

	public void setMessageThread(MessageThread messageThread) {
		this.messageThread = messageThread;
	}

	@Type(type = "true_false")
	@Column(name="deleted", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Column(name = "show_messages_from", columnDefinition="DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getShowMessagesFrom() {
		return showMessagesFrom;
	}

	public void setShowMessagesFrom(Date showMessagesFrom) {
		this.showMessagesFrom = showMessagesFrom;
	}

	@Override
	public String toString() {
		return "ThreadParticipant [id=" + id + ", read=" + read + ", archived=" + archived + ", user=" + user
				+ ", lastReadMessage=" + lastReadMessage + ", messageThread=" + messageThread + ", deleted=" + deleted
				+ ", showMessagesFrom=" + showMessagesFrom + "]";
	}
	
}
