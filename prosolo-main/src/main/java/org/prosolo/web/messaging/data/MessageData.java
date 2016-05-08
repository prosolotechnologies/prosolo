package org.prosolo.web.messaging.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.data.UserDataFactory;

public class MessageData implements Serializable, Comparable<MessageData> {
	
	private static final long serialVersionUID = 6920914506792650170L;

	private long id;
	private long threadId;
	private boolean readed;
	private Date created;
	private UserData actor;
	private String date;
	private String message;
	private String createdTimeValue;

	public MessageData(Message message, User user) {
		this.id = message.getId();
		this.threadId = message.getMessageThread().getId();
		this.actor = UserDataFactory.createUserData(message.getSender().getUser());
		this.message = message.getContent();
		this.readed = checkIfRead(message, user);
		this.created = message.getCreatedTimestamp();
		this.createdTimeValue = DateUtil.createUpdateTime(this.created);
		
		String timeCreated = null;
		
		if (DateUtil.daysBetween(message.getDateCreated(), new Date()) == 0) {
			timeCreated = DateUtil.getTimeAgoFromNow(message.getDateCreated());
		} else {
			timeCreated = DateUtil.getPrettyDate(message.getDateCreated());
		}
		this.date = timeCreated;
	}
	
	public MessageData(Message message, User user, boolean read) {
		this.id = message.getId();
		this.threadId = message.getMessageThread().getId();
		this.actor = UserDataFactory.createUserData(message.getSender().getUser());
		this.message = message.getContent();
		this.readed = read;
		this.created = message.getCreatedTimestamp();
		this.createdTimeValue = DateUtil.createUpdateTime(this.created);
		
		String timeCreated = null;
		
		if (DateUtil.daysBetween(message.getDateCreated(), new Date()) == 0) {
			timeCreated = DateUtil.getTimeAgoFromNow(message.getDateCreated());
		} else {
			timeCreated = DateUtil.getPrettyDate(message.getDateCreated());
		}
		this.date = timeCreated;
	}
	
	private boolean checkIfRead(Message message, User user) {
		//TODO how to check if this message is read, by using User entity?
		return false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private Message resource;

	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}

	public String getContent() {
		return this.getMessage();
	}

	public String getShortContent() {
		String shortContent = this.getMessage();
		int length = 30;
		
		if (shortContent != null && shortContent.length() > length) {
			shortContent = shortContent.substring(0, length) + "...";
		}
		return shortContent;
	}

	public Message getResource() {
		return resource;
	}

	public void setResource(Message resource) {
		this.resource = resource;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	
	public UserData getActor() {
		return actor;
	}

	public void setActor(UserData actor) {
		this.actor = actor;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getCreatedTimeValue() {
		return createdTimeValue;
	}

	public void setCreatedTimeValue(String createdTimeValue) {
		this.createdTimeValue = createdTimeValue;
	}

	@Override
	public int compareTo(MessageData o) {
		return o.getCreated().compareTo(this.getCreated());
	}
}
