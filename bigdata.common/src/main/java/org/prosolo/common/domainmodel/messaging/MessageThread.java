package org.prosolo.common.domainmodel.messaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;
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
	private Set<ThreadParticipant> participants;
	private List<Message> messages;

	public MessageThread() {
		setParticipants(new HashSet<>());
		setMessages(new ArrayList<>());
	}

	@ManyToOne
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	@OneToMany(cascade=CascadeType.ALL, mappedBy="messageThread",fetch= FetchType.LAZY)
	public Set<ThreadParticipant> getParticipants() {
		return participants;
	}
	
	public ThreadParticipant getParticipant(long userId) {
		for (ThreadParticipant participant : participants) {
			if (participant.getUser().getId() == userId) {
				return participant;
			}
		}
		return null;
	}

	public void setParticipants(Set<ThreadParticipant> participants2) {
		this.participants = participants2;
	}
	
	public void addParticipant(ThreadParticipant participant) {
		if (participant != null)
			this.participants.add(participant);
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

	@OneToMany(mappedBy="messageThread", fetch= FetchType.LAZY)
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
