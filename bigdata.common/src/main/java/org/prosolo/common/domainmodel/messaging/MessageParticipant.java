package org.prosolo.common.domainmodel.messaging;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.user.User;

@Entity
public class MessageParticipant implements Serializable {

	private static final long serialVersionUID = 1156059969948940348L;
	
	private long id;
	private boolean sender;
	private boolean read;
	private User participant;
	
	@Id
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Type(type = "long")
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	@Type(type = "true_false")
	@Column(name="sender", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isSender() {
		return sender;
	}

	public void setSender(boolean sender) {
		this.sender = sender;
	}

	@Type(type = "true_false")
	@Column(name="is_read", columnDefinition = "char(1) DEFAULT 'F'")
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
	
}
