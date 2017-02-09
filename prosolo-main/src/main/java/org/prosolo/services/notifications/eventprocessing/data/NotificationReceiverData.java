package org.prosolo.services.notifications.eventprocessing.data;

import java.io.Serializable;

public class NotificationReceiverData implements Serializable {

	private static final long serialVersionUID = -6367624479363120368L;

	private final long receiverId;
	private final String notificationLink;
	private final boolean objectOwner;
	
	public NotificationReceiverData(long receiverId, String notificationLink, boolean objectOwner) {
		this.receiverId = receiverId;
		this.notificationLink = notificationLink;
		this.objectOwner = objectOwner;
	}

	public long getReceiverId() {
		return receiverId;
	}

	public String getNotificationLink() {
		return notificationLink;
	}

	public boolean isObjectOwner() {
		return objectOwner;
	}
	
}
