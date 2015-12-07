package org.prosolo.web.communications.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.domainmodel.user.MessageParticipant;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
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

	public MessageData(SimpleOfflineMessage message, User user) {
		this.id = message.getId();
		this.threadId = message.getMessageThread().getId();
		this.actor = UserDataFactory.createUserData(message.getSender().getParticipant());
		this.message = message.getContent();
		this.readed = checkIfRead(message, user);
		this.created = message.getDateCreated();
		
		String timeCreated = null;
		
		if (DateUtil.daysBetween(message.getDateCreated(), new Date()) == 0) {
			timeCreated = DateUtil.getTimeAgoFromNow(message.getDateCreated());
		} else {
			timeCreated = DateUtil.getPrettyDate(message.getDateCreated());
		}
		this.date = timeCreated;
	}
	
	private boolean checkIfRead(SimpleOfflineMessage message, User user) {
		for(MessageParticipant mp : message.getParticipants()) {
			if(mp.getParticipant().equals(user)) {
				return mp.isRead();
			}
		}
		return false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private SimpleOfflineMessage resource;

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
		int length = 100;
		
		if (shortContent != null && shortContent.length() > length) {
			shortContent = shortContent.substring(0, length) + "...";
		}
		return shortContent;
	}

	public SimpleOfflineMessage getResource() {
		return resource;
	}

	public void setResource(SimpleOfflineMessage resource) {
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
	
	@Override
	public int compareTo(MessageData o) {
		return o.getCreated().compareTo(this.getCreated());
	}
}
