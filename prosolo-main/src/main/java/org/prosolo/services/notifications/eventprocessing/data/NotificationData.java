package org.prosolo.services.notifications.eventprocessing.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.UserData;

public class NotificationData implements Serializable {

	private static final long serialVersionUID = -4934550581087302253L;
	
	private long id;
	private boolean read;
	private Date date;
	private NotificationType notificationType;
	
	private UserData actor;
	private UserData receiver;
	private String link;
	private ObjectType objectType;
	private String objectTitle;
	private String predicate;

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

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getDatePretty() {
		return DateUtil.getTimeAgoFromNow(date);
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public UserData getActor() {
		return actor;
	}

	public void setActor(UserData actor) {
		this.actor = actor;
	}

	public ObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(ObjectType objectType) {
		this.objectType = objectType;
	}

	public String getObjectTitle() {
		return objectTitle;
	}

	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}

	public UserData getReceiver() {
		return receiver;
	}

	public void setReceiver(UserData receiver) {
		this.receiver = receiver;
	} 

}
