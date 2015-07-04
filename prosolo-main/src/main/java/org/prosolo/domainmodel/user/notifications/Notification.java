package org.prosolo.domainmodel.user.notifications;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.prosolo.domainmodel.activities.events.EventType;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.user.notifications.NotificationAction;

//@ManagedBean(name = "notification")
@Entity
//@Table(name="user_Notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Notification extends BaseEntity { 

	private static final long serialVersionUID = -7422502818042864822L;
	
	private EventType type;
	private boolean actinable;
	private boolean read;
	private String message;
	private List<NotificationAction> actions;
	private NotificationAction chosenAction;
	private User receiver;
	
	/**
	 * User who has created the event.
	 */
	private User actor;
	
	/**
	 * Object on which the event is created on.
	 */
	//private BaseEntity object;
	
 	@OneToOne
	public User getActor() {
		return actor;
	}

	public void setActor(User actor) {
		this.actor = actor;
	}
	
	public Notification() {
		actions = new ArrayList<NotificationAction>();
	}
	
	@Enumerated(EnumType.STRING)
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}
	//@Column(name="actinable")
	@Column(nullable=true)
	@Type(type="true_false")
	public boolean isActinable() {
		return actinable;
	}
	
	public void setActinable(boolean actinable) {
		this.actinable = actinable;
	}
	
	@Column(name="readed", nullable=true)
	@Type(type="true_false")
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	
	@Column(length = 90000)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@ElementCollection(targetClass=NotificationAction.class, fetch=FetchType.EAGER)
	@CollectionTable(name = "user_Notification_Actions")
	@Enumerated(EnumType.STRING)
	public List<NotificationAction> getActions() {
		return actions;
	}

	public void setActions(List<NotificationAction> actions) {
		this.actions = actions;
	}

	public void addAction(NotificationAction action) {
		getActions().add(action);
	}

	@Enumerated(EnumType.STRING)
	public NotificationAction getChosenAction() {
		return chosenAction;
	}

	public void setChosenAction(NotificationAction chosenAction) {
		this.chosenAction = chosenAction;
	}
	
	@OneToOne (fetch = FetchType.LAZY)
	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	@Transient
	public BaseEntity getObject() {
		return null;
	}
	
}
