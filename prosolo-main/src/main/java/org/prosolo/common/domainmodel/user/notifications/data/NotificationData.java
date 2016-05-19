package org.prosolo.common.domainmodel.user.notifications.data;

import java.io.Serializable;
import java.util.Date;
import org.prosolo.common.domainmodel.user.notifications.NotificationType;

public class NotificationData implements Serializable {

	private static final long serialVersionUID = -4934550581087302253L;
	
	private long id;
	private boolean read;
	private Date created;
	private String type;
	private NotificationType notificationType;
	
	//private org.prosolo.services.nodes.data.UserData actor;
	private String date;
	private String link;
	private String notificationText;

	public NotificationData() {
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getNotificationText() {
		return notificationText;
	}

	public void setNotificationText(String notificationText) {
		this.notificationText = notificationText;
	} 

}
