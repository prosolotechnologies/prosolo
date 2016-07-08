package org.prosolo.services.interaction;

import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.messaging.data.MessagesThreadData;

public interface MessagingManager extends AbstractManager {
	
	Message sendMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context) throws ResourceCouldNotBeLoadedException;
	
	List<Message> getMessagesForThread(long threadId, int page, int limit, Date fromTime);
	
	public MessageThread createNewMessagesThread(long creatorId, List<Long> participantIds, String subject) throws ResourceCouldNotBeLoadedException;

	List<MessagesThreadData> convertMessagesThreadsToMessagesThreadData(List<MessageThread> mThreads, long userId);

	MessagesThreadData convertMessagesThreadToMessagesThreadData(
			MessageThread mThread, long userId);

	List<MessageThread> getLatestUserMessagesThreads(long userId, int page, int limit, boolean archived);

	boolean markThreadAsRead(long threadId, long userId);

	MessageThread getLatestMessageThread(long userId, boolean archived);
	
	public Message sendMessage(long senderId,  long recieverId, String msg) throws DbConnectionException;
	
	public ThreadParticipant findParticipation(long threadId, long userId);
	
	public List<Message> getUnreadMessages(long threadId, Message lastReadMessage, Date fromTime);
	
	public List<Message> getMessagesBeforeMessage(long threadId, Message message, int numberOfMessages, Date fromTime);
	
	public void archiveThread(long threadId, long userId);

	void markThreadDeleted(long threadId, long userId);

	List<MessageThread> getUnreadMessageThreads(long id);

}
