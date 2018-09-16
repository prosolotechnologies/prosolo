package org.prosolo.web.messaging.data;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.UserData;

import java.io.Serializable;
import java.util.Date;

public class MessageData implements Serializable, Comparable<MessageData> {
	
	private static final long serialVersionUID = 6920914506792650170L;

	private long id;
	private long threadId;
	private boolean readed = false;
	private Date created;
	private UserData actor;
	private String message;

	public MessageData(Message message) {
		this.id = message.getId();
		this.threadId = message.getMessageThread().getId();
		this.actor = new UserData(message.getSender().getUser());
		this.message = message.getContent();
		this.created = message.getCreatedTimestamp();
		this.readed = false;
	}
	
	public MessageData(Message message, boolean read) {
		this(message);
		this.readed = read;
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
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public long getCreatedTime() {
		return DateUtil.getMillisFromDate(created);
	}

	@Override
	public int compareTo(MessageData o) {
		return o.getCreated().compareTo(this.getCreated());
	}
}
