package org.prosolo.services.interaction;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.Pair;
import org.prosolo.services.data.Result;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessageThreadData;
import org.prosolo.web.messaging.data.MessageThreadParticipantData;

import java.util.List;
import java.util.Optional;

public interface MessagingManager extends AbstractManager {

	/**
	 * Starts a new conversation, i.e. creates a new message thread between creator and message participants.
	 *
	 * @param creatorId user starting a message thread
	 * @param participantIds participants in the message thread
	 * @param subject message thread subject. It is not used on the interface at this time
	 * @return message thread instance (event data is included)
	 * @throws ResourceCouldNotBeLoadedException if any of the participants is not found
	 */
	Result<MessageThread> createNewMessageThread(long creatorId, List<Long> participantIds, String subject) throws ResourceCouldNotBeLoadedException;

	/**
	 * Retrieves user message threads ordered descending by the last update date. The method will always fetch one more
	 * message thread than the limit, so that this information can be used to determine whether there are more message
	 * threads to load.
	 *
	 * @param userId user id
	 * @param page page for pagination
	 * @param limit if limit is 0, then pagination is not used, but all message threads are loaded
	 * @param archived whether to show archived messages (is set true) or not archived (if set false)
	 * @return list of message threads data
	 */
	List<MessageThreadData> getMessageThreads(long userId, int page, int limit, boolean archived);

	/**
	 * Marks the message thread as read for the given user.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 * @param context context sent with event
	 * @return message thread data
	 * @throws DbConnectionException if there was an exception updating the appropriate {@link org.prosolo.common.domainmodel.messaging.ThreadParticipant} instance.
	 */
	MessageThreadData markThreadAsRead(long threadId, long userId, UserContextData context) throws DbConnectionException;

	/**
	 * Marks the message thread as read for the given user. Returns it with event data.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 * @param context context sent with event
	 * @return message thread data (with event data)
	 * @throws DbConnectionException if there was an exception updating the appropriate {@link org.prosolo.common.domainmodel.messaging.ThreadParticipant} instance.
	 */
	Result<MessageThreadData> markThreadAsReadAndGetEvents(long threadId, long userId, UserContextData context) throws DbConnectionException;

	/**
	 * Sends a message from a sender to a receiver. If messageThreadId is unknown (value is 0), then the message thread for these two users will be found if exists or new one will be created.
	 *
	 * @param threadId message thread id
	 * @param senderId sender id
	 * @param receiverId id of the user receiving the message
	 * @param text message contencts
	 * @param contextData context
	 * @return message created
	 * @throws DbConnectionException
	 */
	MessageData sendMessage(long threadId, long senderId, long receiverId, String text, UserContextData contextData)
			throws DbConnectionException;

	Pair<MessageData, MessageThreadData> sendMessageAndReturnMessageAndThread(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException;

	Result<Pair<MessageData, MessageThreadData>> sendMessageAndGetEvents(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException;

	/**
	 * Retrieves unread messages for the user in the message thread. If user did not read this thread before, all
	 * messages from the thread will be fetched. If user did see the thread before, then only messages that have been
	 * created after the time the thread was last seen will be loaded.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 * @return list of unread messages
	 */
	List<MessageData> getAllUnreadMessages(long threadId, long userId);

	/**
	 * Retrieves read messages for the given user in the message thread. The method will always fetch one more message
	 * than the limit so that this information can be used to determine whether there are more messages to load.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 * @param page page for pagination
	 * @param limit number of messages to retrieve
	 * @return list of {@link MessageData} instances.
	 */
	List<MessageData> getReadMessages(long threadId, long userId, int page, int limit);

	/**
	 * If archiveStatus is true, updates the message thread for the given user to be archived. If archiveStatus is
	 * false, the message thread for the given user is revoked from being archived.
	 *
	 * @param threadId id of the message thread
	 * @param userId id of the user participating in the thread
	 * @param archiveStatus determines whether a thread should be archived or revoked from being archived
	 */
	void updateArchiveStatus(long threadId, long userId, boolean archiveStatus);

	/**
	 * Marks the thread as deleted, but does not delete it. The reason it is not deleted is that when a new message
	 * between the users is exchanged, this message thread is revoked, along with all its messages.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 */
	void markThreadDeleted(long threadId, long userId);

	/**
	 * Returns a boolean value denoting whether user has any unread message in any message thread.
	 *
	 * @param userId user id
	 * @return true if user has an unread message in any message thread, false otherwise
	 */
	boolean hasUserUnreadMessages(long userId);

	/**
	 * Retrieves message thread data.
	 *
	 * @param threadId message thread id
	 * @param userId user id
	 * @return message thread data
	 */
	MessageThreadData getMessageThread(long threadId, long userId);

	/**
	 * Retrieves a message thread between two users.
	 *
	 * @param loggedUserId id of the logged user
	 * @param otherUserId id of the other user
	 * @return message thread
	 */
	MessageThread getMessageThreadForUsers(long loggedUserId, long otherUserId);

	/**
	 * Retrieves a message thread data for a message thread between two users.
	 *
	 * @param loggedUserId id of the logged user
	 * @param otherUserId id of the other user
	 * @return message thread data
	 */
	Optional<MessageThreadData> getMessageThreadDataForUsers(long loggedUserId, long otherUserId);

	/**
	 * Retrieves id of a user who sent a message.
	 *
	 * @param messageId id of a message
	 * @param session session within which to execute a query
	 * @return id of a user who sent the message
	 */
    long getSenderId(long messageId, Session session);

	/**
	 * Returns a list of participants of a message thread the message belongs to.
	 *
	 * @param messageId id of a message
	 * @param session session within which to execute a query
	 * @return list of thread participants
	 */
	List<MessageThreadParticipantData> getThreadParticipantsForMessage(long messageId, Session session);
}