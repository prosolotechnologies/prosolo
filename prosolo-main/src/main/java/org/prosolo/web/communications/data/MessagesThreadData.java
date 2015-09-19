package org.prosolo.web.communications.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.activitywall.data.UserDataFactory;

/*
 * @author Zoran Jeremic 2013-05-19
 */
public class MessagesThreadData implements Serializable {
	
	private static final long serialVersionUID = -5533560146824083368L;

	private boolean unreaded;
	private String subject;
	private long id;
	private Date lastUpdated;
	private String updateTime;
	private List<MessageData> messages;
	private List<UserData> participants;
	private List<UserData> participantsWithoutLoggedUser;
	private String participantsList;
	private String participantsListWithoutLoggedUser;
	private MessageData latestReceived;
	private boolean readed;
	
	public MessagesThreadData() {
		messages = new ArrayList<MessageData>();
		participants = new ArrayList<UserData>();
		participantsWithoutLoggedUser = new ArrayList<UserData>();
	}
	
	public MessagesThreadData(MessagesThread thread, User loggedUser) {
		this();
		
		this.id = thread.getId();
		this.subject = thread.getSubject();
		this.lastUpdated = thread.getLastUpdated();
		this.updateTime = DateUtil.createUpdateTime(lastUpdated);
		
		for (User user : thread.getParticipants()) {
			UserData userData = UserDataFactory.createUserData(user);
			
			participants.add(userData);
			
			if (user.getId() != loggedUser.getId()) {
				participantsWithoutLoggedUser.add(userData);
			}
		}
		
		Collections.sort(participants);
		Collections.sort(participantsWithoutLoggedUser);
		
		int i = 0;
		StringBuffer buffer = new StringBuffer();
		
		for (UserData userData : participantsWithoutLoggedUser) {
			buffer.append(userData.getName());
			
			if (i > 0) {
				buffer.append(", ");
			}
			
			i++;
		}
		this.participantsListWithoutLoggedUser = buffer.toString();
		
		List<MessageData> messagesData = new ArrayList<MessageData>();
		
		for (SimpleOfflineMessage m : thread.getMessages()) {
			messagesData.add(new MessageData(m));
		}
		this.messages = messagesData;
	}
	
	public MessageData getLatest() {
		if (!messages.isEmpty()) {
			return messages.get(messages.size() - 1);
		} else
			return null;
	}

	public String getParticipantsList() {
		return participantsList;
	}

	public void setParticipantsList(String participantsList) {
		this.participantsList = participantsList;
	}

	public String getUpdateTime() {
		return updateTime;
	}
	
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
	public boolean isUnreaded() {
		return unreaded;
	}

	public void setUnreaded(boolean unreaded) {
		this.unreaded = unreaded;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<MessageData> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageData> messages) {
		this.messages = messages;
	}
	
	public void addMessage(MessageData message) {
		if (!this.messages.contains(message)) {
			this.messages.add(message);
		}
	}

	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}
	
	public MessageData getLatestReceived() {
		return latestReceived;
	}
	
	public void setLatestReceived(MessageData lReceived) {
		this.latestReceived = lReceived;
	}

	public List<UserData> getParticipants() {
		return participants;
	}

	public void setParticipants(List<UserData> participants) {
		this.participants = participants;
	}
	
	public boolean containsParticipant(long userId) {
		for (UserData u : participants) {
			if (u.getId() == userId) {
				return true;
			}
		}
		return false;
	}

	public List<UserData> getParticipantsWithoutLoggedUser() {
		return participantsWithoutLoggedUser;
	}

	public void setParticipantsWithoutLoggedUser(List<UserData> participantsWithoutLoggedUser) {
		this.participantsWithoutLoggedUser = participantsWithoutLoggedUser;
	}

	public String getParticipantsListWithoutLoggedUser() {
		return participantsListWithoutLoggedUser;
	}

	public void setParticipantsListWithoutLoggedUser(String participantsListWithoutLoggedUser) {
		this.participantsListWithoutLoggedUser = participantsListWithoutLoggedUser;
	}
	
}

