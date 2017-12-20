package org.prosolo.services.notifications.eventprocessing.data;

import org.prosolo.web.util.page.PageSection;

import java.io.Serializable;

public class NotificationReceiverData implements Serializable {

	private static final long serialVersionUID = -6367624479363120368L;

	private final long receiverId;
	private final String notificationLink;
	private final boolean objectOwner;
	private PageSection pageSection;
	
	public NotificationReceiverData(long receiverId, String notificationLink, boolean objectOwner, PageSection pageSection) {
		this.receiverId = receiverId;
		this.notificationLink = notificationLink;
		this.objectOwner = objectOwner;
		this.pageSection = pageSection;
	}

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

	public PageSection getPageSection() {
		return pageSection;
	}

	public void setPageSection(PageSection pageSection) {
		this.pageSection = pageSection;
	}
}
