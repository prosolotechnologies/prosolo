package org.prosolo.common.domainmodel.user.notifications;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;


@Entity
public class Notification1 extends BaseEntity { 
	
	private static final long serialVersionUID = 6241709766851863401L;
	
	private NotificationType type;
	private boolean read;
	/**
	 * User who has created the event.
	 */
	private User actor;
	private User receiver;
	private String link;
	/*
	 * Next two fields are there to allow us to get
	 * object title if needed for given notification type.
	 * ObjectId should not be set if object title is not needed
	 * for notification while objectType should always be set.
	 */
	private ResourceType objectType;
	private long objectId;
	/*
	 * indicates if user is owner of object of this notification.
	 */
	private boolean objectOwner;
	private ResourceType targetType;
	private long targetId;
	private boolean notifyByEmail;
	private NotificationSection section;

	/*
	currently this flag refers to all users in notification: actor, object, target
	 */
	private boolean anonymizedActor;
	private NotificationActorRole notificationActorRole = NotificationActorRole.OTHER;
	
	public Notification1() {
		
	}
	
	@ManyToOne
	public User getActor() {
		return actor;
	}

	public void setActor(User actor) {
		this.actor = actor;
	}
	
	@Type(type = "true_false")
	@Column(name="is_read", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'T'")
	public boolean isNotifyByEmail() {
		return notifyByEmail;
	}
	
	public void setNotifyByEmail(boolean notifyByEmail) {
		this.notifyByEmail = notifyByEmail;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@Enumerated(EnumType.STRING)
	public ResourceType getObjectType() {
		return objectType;
	}

	public void setObjectType(ResourceType objectType) {
		this.objectType = objectType;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	@Enumerated(EnumType.STRING)
	public ResourceType getTargetType() {
		return targetType;
	}

	public void setTargetType(ResourceType targetType) {
		this.targetType = targetType;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isObjectOwner() {
		return objectOwner;
	}

	public void setObjectOwner(boolean objectOwner) {
		this.objectOwner = objectOwner;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public NotificationSection getSection() {
		return section;
	}

	public void setSection(NotificationSection section) {
		this.section = section;
	}

	public boolean isAnonymizedActor() {
		return anonymizedActor;
	}

	public void setAnonymizedActor(boolean anonymizedActor) {
		this.anonymizedActor = anonymizedActor;
	}

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	public NotificationActorRole getNotificationActorRole() {
		return notificationActorRole;
	}

	public void setNotificationActorRole(NotificationActorRole notificationActorRole) {
		this.notificationActorRole = notificationActorRole;
	}
}
