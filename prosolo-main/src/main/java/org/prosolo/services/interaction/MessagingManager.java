package org.prosolo.services.interaction;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;

import java.util.Date;
import java.util.List;

public interface MessagingManager extends AbstractManager {

//	void sendMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context, UserContextData contextData)
//			throws ResourceCouldNotBeLoadedException;
//
//	Result<Void> sendMessagesAndGetEvents(long senderId, List<UserData> receivers, String text, Long threadId, String context, UserContextData contextData)
//			throws ResourceCouldNotBeLoadedException;

	List<Message> getMessagesForThread(long threadId, int page, int limit, Date fromTime);

	Result<MessageThread> createNewMessageThread(long creatorId, List<Long> participantIds, String subject) throws ResourceCouldNotBeLoadedException;

	/**
	 * Retrieves user message threads ordered descending by the last update date.
	 *
	 * @param userId
	 * @param page
	 * @param limit if limit is 0, then pagination is not used, but all message threads are loaded
	 * @param archived
	 * @return
	 */
	List<MessagesThreadData> getMessageThreads(long userId, int page, int limit, boolean archived);

	/**
	 * Marks the message thread to be read for the given user. Also, returns the data about the thread.
	 *
	 * @param threadId
	 * @param userId
	 * @param context
	 * @return
	 * @throws DbConnectionException
	 */
	MessagesThreadData markThreadAsRead(long threadId, long userId, UserContextData context) throws DbConnectionException;

	Result<MessagesThreadData> markThreadAsReadAndGetEvents(long threadId, long userId, UserContextData context) throws DbConnectionException;

//	MessageThread getLatestMessageThread(long userId, boolean archived, UserContextData context);

//	Result<MessageThread> getLatestMessageThreadAndGetEvents(long userId, boolean archived, UserContextData context)
//			throws DbConnectionException;

	//Result<Message> sendMessageAndGetEvents(UserContextData context, long recieverId, String msg) throws DbConnectionException;

	//Message sendMessage1(UserContextData context, long recieverId, String msg) throws DbConnectionException, EventException;

//	String sendMessageDialog(long senderId, long receiverId, String msg, UserContextData contextData)
//			throws DbConnectionException;
//
//	Result<String> sendMessageDialogAndGetEvents(long senderId, long receiverId, String msg, UserContextData contextData)
//			throws DbConnectionException;

//	Message sendMessage1(long senderId, long recieverId, String msg) throws DbConnectionException;

	/**
	 * Sends a message from a sender to a receiver. If messageThreadId is unknown (value is 0), then the message thread for these two users will be found if exists or new one will be created.
	 *
	 * @param threadId
	 * @param senderId
	 * @param receiverId
	 * @param msg
	 * @param contextData
	 * @return
	 * @throws DbConnectionException
	 */
	MessageData sendMessage(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException;

	Object[] sendMessageAndReturnMessageAndThread(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException;

	Result<Object[]> sendMessageAndGetEvents(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException;

	ThreadParticipant getThreadParticipant(long threadId, long userId);

	/**
	 * Retrieves unread messages for the user in the message thread. If user did not read this thread before, all
	 * messages from the thread will be fetched. If user did see the thread before, then only messages that have been
	 * created after the time the thread was last seen will be loaded.
	 *
	 * @param threadId
	 * @param userId
	 * @return list of unread messages
	 */
	List<MessageData> getAllUnreadMessages(long threadId, long userId);

	/**
	 * Retrieves read messages for the given user in the message thread. THe method will always fetch one more message
	 * than the limit so that this information can be used to determine whether there are more messages to load.
	 *
	 * @param threadId
	 * @param userId
	 * @param page
	 * @param limit
	 * @return
	 */
	List<MessageData> getReadMessages(long threadId, long userId, int page, int limit);

	void archiveThread(long threadId, long userId);

	void markThreadDeleted(long threadId, long userId);

	/**
	 * Returns true if user has any unread message from any message thread. Otherwise, the method returns false.
	 * @param userId
	 * @return
	 */
	boolean userHasUnreadMessages(long userId);

	/**
	 * Retrieves message thread data.
	 *
	 * @param threadId
	 * @param userId
	 * @return thread data
	 */
	MessagesThreadData getMessageThread(long threadId, long userId);

}