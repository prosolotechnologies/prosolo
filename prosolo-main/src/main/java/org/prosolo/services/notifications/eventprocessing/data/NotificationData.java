package org.prosolo.services.notifications.eventprocessing.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.user.notifications.NotificationType;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.util.page.PageSection;

public class NotificationData implements Serializable {

	private static final long serialVersionUID = -4934550581087302253L;
	
	private long id;
	private boolean read;
	private Date date;
	private NotificationType notificationType;
	
	private UserData actor;
	private UserData receiver;
	private String link;
	private long objectId;
	private ResourceType objectType;
	private String objectTitle;
	private String predicate;
	private long targetId;
	private ResourceType targetType;
	private String targetTitle = "";
	private String relationToTarget = "";
	private PageSection section;

	public NotificationData() { }

	public long getNotificationTime() {
		return DateUtil.getMillisFromDate(date);
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public UserData getActor() {
		return actor;
	}

	public void setActor(UserData actor) {
		this.actor = actor;
	}

	public UserData getReceiver() {
		return receiver;
	}

	public void setReceiver(UserData receiver) {
		this.receiver = receiver;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public ResourceType getObjectType() {
		return objectType;
	}

	public void setObjectType(ResourceType objectType) {
		this.objectType = objectType;
	}

	public String getObjectTitle() {
		return objectTitle;
	}

	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	public ResourceType getTargetType() {
		return targetType;
	}

	public void setTargetType(ResourceType targetType) {
		this.targetType = targetType;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getRelationToTarget() {
		return relationToTarget;
	}

	public void setRelationToTarget(String relationToTarget) {
		this.relationToTarget = relationToTarget;
	}

	public PageSection getSection() {
		return section;
	}

	public void setSection(PageSection section) {
		this.section = section;
	}
}
