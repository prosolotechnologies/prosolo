package org.prosolo.services.interaction;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.web.communications.data.MessagesThreadData;

public interface MessagingManager extends AbstractManager {
	
//	List<SimpleOfflineMessage> sendMessages(long senderId, List<UserData> receivers, String text, long threadId, String context) throws ResourceCouldNotBeLoadedException;

	Message sendMessages(long senderId, List<UserData> receivers, String text, long threadId, String context) throws ResourceCouldNotBeLoadedException;
	
	Message sendSimpleOfflineMessage(long senderId, long receiverId,
			String content, long threadId, String context) throws ResourceCouldNotBeLoadedException;

	Message sendSimpleOfflineMessage(User sender, long receiverId,
			String content, MessageThread messagesThread, String context) throws ResourceCouldNotBeLoadedException;
	
	List<Message> getUnreadSimpleOfflineMessages(User user,
			int page, int limit);

	List<Message> getSimpleOfflineMessages(User user, int page, int limit);

	List<MessageThread> getUserMessagesThreads(User user, int page, int limit);

	MessageThread getMessagesThreadForPrevoiusMessage(
			Message previousMessage);

	MessageThread createNewMessagesThread(User creator, List<Long> participantIds, String subject);

	List<Message> getMessagesForThread(long threadId, int page, int limit);

	List<MessagesThreadData> convertMessagesThreadsToMessagesThreadData(List<MessageThread> mThreads, User user);

	MessagesThreadData convertMessagesThreadToMessagesThreadData(
			MessageThread mThread, User user);

	//boolean markAsRead(long[] messageIds, User user, Session session) throws ResourceCouldNotBeLoadedException;

	Message markAsRead(Message message, User user, Session session);

	MessageThread findMessagesThreadForUsers(long user1Id, long user2Id);

	List<MessageThread> getLatestUserMessagesThreads(User user, int page, int limit);

	boolean markThreadAsRead(long threadId, User user);

	List<Message> getMessagesForThread(MessageThread thread,
			int page, int limit, Session session);

	MessageThread getLatestMessageThread(User user);
	
	public Message sendMessage(long senderId, long receiverId, String msg) throws DbConnectionException;

}
