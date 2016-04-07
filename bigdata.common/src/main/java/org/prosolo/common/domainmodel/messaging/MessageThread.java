package org.prosolo.common.domainmodel.messaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.user.User;


/**
 * @author "Zoran Jeremic"
 * @date 2013-04-16
 */
@Entity
//@Table(name = "user_MessagesThread")
public class MessageThread extends BaseEntity { 

	private static final long serialVersionUID = -1640160399285990926L;

	private User creator;
	private Date dateStarted;
	private Date lastUpdated;
	private String subject;
	private List<User> participants;
	private List<Message> messages;

	public MessageThread() {
		setParticipants(new ArrayList<User>());
		setMessages(new ArrayList<Message>());
	}

	@ManyToOne
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	@ManyToMany
	@Cascade({ org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH })
	@JoinTable(name = "user_MessageThread_participants_User")
	public List<User> getParticipants() {
		return participants;
	}

	public void setParticipants(List<User> participants2) {
		this.participants = participants2;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "started", length = 19)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", length = 19)
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@OneToMany(mappedBy="messageThread")
	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public void addMessage(Message message) {
		if (message != null) {
			this.messages.add(message);
			this.lastUpdated=new Date();
		}
	}

	@Column(name = "subject", nullable = true)
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
