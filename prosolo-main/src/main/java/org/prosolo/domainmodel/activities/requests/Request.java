package org.prosolo.domainmodel.activities.requests;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.activities.requests.Request;
import org.prosolo.domainmodel.activities.requests.RequestStatus;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Request extends BaseEntity {
	
	private static final long serialVersionUID = -6388315238963447689L;
	
	private User sentTo;
	private String comment;
	private RequestStatus status;
	private EventType requestType;
	private User maker;

	private Request basedOn;

	@Transient
	public abstract BaseEntity getResource();
	
	public abstract void setResource(BaseEntity resource);
	
	@Enumerated(EnumType.STRING)
	public EventType getRequestType() {
		return requestType;
	}

	public void setRequestType(EventType requestType) {
		this.requestType = requestType;
	}

	@Enumerated(EnumType.STRING)
	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	@Column(nullable = true, length = 90000)
	public String getComment() {
		return comment;
	}

	public void setComment(String text) {
		this.comment = text;
	}

	@OneToOne
	public User getSentTo() {
		return sentTo;
	}

	public void setSentTo(User sentTo) {
		this.sentTo = sentTo;
	}
 
	@OneToOne(fetch = FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		if (null != maker) {
			this.maker = maker;
		}
	}

	@OneToOne(fetch = FetchType.LAZY)
	public Request getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(Request basedOn) {
		this.basedOn = basedOn;
	}

}
