package org.prosolo.web.messaging.data;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.services.nodes.data.UserData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * @author Zoran Jeremic 2013-05-19
 */
public class MessagesThreadData implements Serializable {
	
	private static final long serialVersionUID = -5533560146824083368L;

	private String subject;
	private long id;
	private List<MessageData> messages;
	private List<MessagesThreadParticipantData> participants;
	private MessagesThreadParticipantData receiver;
	private MessageData lastUpdated;
	private boolean readed;
	
	public MessagesThreadData() {
		messages = new ArrayList<>();
		participants = new ArrayList<>();
	}
	
	public MessagesThreadData(MessageThread thread, long userId) {
		this();
		
		this.id = thread.getId();
		this.subject = thread.getSubject();

		ThreadParticipant participant = thread.getParticipant(userId);
		this.readed = participant.isRead();


		this.participants = thread.getParticipants().stream().map(tp -> new MessagesThreadParticipantData(tp)).collect(Collectors.toList());
		this.receiver = thread.getParticipants().stream().filter(tp -> tp.getUser().getId() != userId).map(tp -> new MessagesThreadParticipantData(tp)).findAny().get();

		List<MessageData> messagesData = new ArrayList<>();
		for (Message m : thread.getMessages()) {
			boolean read = false;

			if (participant.getLastReadMessage() != null) {
				read = !m.getCreatedTimestamp().after(participant.getLastReadMessage().getCreatedTimestamp());
			}

			messagesData.add(new MessageData(m, read));
		}
		this.messages = messagesData;

		if (!messages.isEmpty()) {
			this.lastUpdated = messages.get(messages.size() - 1);
		}
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
	
	public MessageData getLastUpdated() {
		return lastUpdated;
	}
	
	public void setLastUpdated(MessageData lReceived) {
		this.lastUpdated = lReceived;
	}

	public List<MessagesThreadParticipantData> getParticipants() {
		return participants;
	}

	public void setParticipants(List<MessagesThreadParticipantData> participants) {
		this.participants = participants;
	}
	
	public MessagesThreadParticipantData getParticipantThatIsNotUser(long userId) {
		for (MessagesThreadParticipantData participant : participants) {
			if (participant.getId() != userId) {
				return participant;
			}
		}
		return null;
	}

	public MessagesThreadParticipantData getReceiver() {
		return receiver;
	}

	public void setReceiver(MessagesThreadParticipantData receiver) {
		this.receiver = receiver;
	}
}

