package org.prosolo.services.interaction.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageParticipant;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.Pair;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessageThreadData;
import org.prosolo.web.messaging.data.MessageThreadParticipantData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service("org.prosolo.services.interaction.MessagingManager")
public class MessagingManagerImpl extends AbstractManagerImpl implements MessagingManager {

	private static final long serialVersionUID = -2828167274273122046L;

	private static Logger logger = Logger.getLogger(MessagingManagerImpl.class);

	@Inject
	private EventFactory eventFactory;
	@Inject
	private UserManager userManager;
	@Inject
	private MessagingManager self;

	@Override
	public MessageData sendMessage(long threadId, long senderId, long receiverId, String text, UserContextData contextData)
			throws DbConnectionException {
		Result<Pair<MessageData, MessageThreadData>> result = self.sendMessageAndGetEvents(threadId, senderId, receiverId, text, contextData);
		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult().getFirst();
	}


	@Override
	public Pair<MessageData, MessageThreadData> sendMessageAndReturnMessageAndThread(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException {
		Result<Pair<MessageData, MessageThreadData>> result = self.sendMessageAndGetEvents(threadId, senderId, receiverId, msg, contextData);
		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<Pair<MessageData, MessageThreadData>> sendMessageAndGetEvents(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException {
		try {
			Result<Pair<MessageData, MessageThreadData>> result = new Result<>();

			MessageThread thread ;

			if (threadId > 0) {
				thread = loadResource(MessageThread.class, threadId);
			} else {
				thread = getMessageThreadForUsers(senderId, receiverId);

				// if there is no existing thread for users, create a new one
				if (thread == null) {
					Result<MessageThread> threadResult = createNewMessageThread(senderId, Arrays.asList(senderId, receiverId), msg);
					thread = threadResult.getResult();
					result.appendEvents(threadResult.getEventQueue());
				}
			}

			// this variable should be created here, after thread is created (if it is newly created)
			Date now = new Date();

			Message message = new Message();
			message.setCreatedTimestamp(now);
			message = saveEntity(message);

			ThreadParticipant msgSender = thread.getParticipant(senderId);
			msgSender.setRead(true);
			msgSender.setLastReadMessage(message);

			//check if thread was deleted by the sender
			if (msgSender.isDeleted()) {
				msgSender.setDeleted(false);
				msgSender.setShowMessagesFrom(now);
			}

			//check if thread was archived by the sender
			msgSender.setArchived(false);

			msgSender = saveEntity(msgSender);

			ThreadParticipant msgReceiver = thread.getParticipant(receiverId);
			msgReceiver.setRead(false);
			//check if thread was deleted by receiver
			if (msgReceiver.isDeleted()) {
				msgReceiver.setDeleted(false);
				msgReceiver.setShowMessagesFrom(now);
			}
			saveEntity(msgReceiver);

			message.setSender(msgSender);
			message.setContent(msg);
			message.setDateCreated(now);
			message.setMessageThread(thread);
			message = saveEntity(message);


			thread.addMessage(message);
			thread.setLastUpdated(now);
			saveEntity(thread);

			Map<String, String> parameters = new HashMap<>();

			parameters.put("context", contextData.getContext().getLearningContext());
			parameters.put("users", thread.getParticipants().stream().map(u ->
					String.valueOf(u.getId())).collect(Collectors.joining(",")));
			parameters.put("user", String.valueOf(thread.getParticipants().iterator().next().getId()));
			parameters.put("message", String.valueOf(message.getId()));

			result.appendEvent(eventFactory.generateEventData(EventType.SEND_MESSAGE, contextData,
					message, null, null, parameters));

			result.setResult(new Pair<>(new MessageData(message, true), new MessageThreadData(thread, senderId)));

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while sending the message");
		}
	}

	@Override
	@Transactional
	public Result<MessageThread> createNewMessageThread(long creatorId, List<Long> participantIds, String subject) throws ResourceCouldNotBeLoadedException {
		Date now = new Date();
		MessageThread messagesThread = new MessageThread();
		messagesThread.setCreator(loadResource(User.class, creatorId));

		List<User> participants = userManager.loadUsers(participantIds);
		if (participants.size() < participantIds.size()) {
			throw new ResourceCouldNotBeLoadedException("Some of the ids : "+participantIds+" do not exist in database, cannot create message thread");
		}

		for (User user : participants) {
			ThreadParticipant participant = new ThreadParticipant();
			participant.setUser(user);
			participant.setShowMessagesFrom(now);
			messagesThread.addParticipant(participant);
			participant.setMessageThread(messagesThread);
		}

		messagesThread.setDateCreated(now);
		messagesThread.setLastUpdated(now);
		messagesThread.setDateStarted(now);

		if (subject.length() > 80) {
			subject = subject.substring(0, 80);
		}
		messagesThread.setSubject(subject);
		messagesThread = saveEntity(messagesThread);

		Result<MessageThread> result = new Result<>();

		result.appendEvent(eventFactory.generateEventData(EventType.START_MESSAGE_THREAD, UserContextData.ofActor(creatorId), messagesThread,
				null, null, null));
		result.setResult(messagesThread);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<MessageThreadData> getMessageThreads(long userId, int page, int limit, boolean archived) {
		try {
			String query =
					"SELECT DISTINCT thread " +
					"FROM MessageThread thread " +
					"LEFT JOIN thread.participants participants " +
					"WHERE :userId IN (participants.user.id) " +
						"AND participants.archived = :archived " +
						"AND participants.deleted = false "	+
					"ORDER BY thread.lastUpdated DESC";

			Query q = persistence.currentManager().createQuery(query)
					.setLong("userId", userId)
					.setBoolean("archived", archived);

			if (limit > 0) {
				q = q.setFirstResult(page * limit)
						.setMaxResults(limit + 1);
			}

			List<MessageThread> result = q.list();

			List<MessageThreadData> messageThreadData = new LinkedList<>();

			if (result != null) {
				for (MessageThread mThread : result) {
					MessageThreadData mtData = new MessageThreadData(mThread, userId);
					messageThreadData.add(mtData);
				}
			}
			return messageThreadData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading message threads");
		}
	}

	@Override
	// nt
	public MessageThreadData markThreadAsRead(long threadId, long userId, UserContextData context) throws DbConnectionException {
		Result<MessageThreadData> res = self.markThreadAsReadAndGetEvents(threadId, userId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<MessageThreadData> markThreadAsReadAndGetEvents(long threadId, long userId, UserContextData context) throws DbConnectionException {
		try {
			String query =
					"SELECT thread, participant, message " +
					"FROM MessageThread thread " +
					"LEFT JOIN thread.messages message " +
					"LEFT JOIN thread.participants participant " +
					"LEFT JOIN participant.user user " +
					"WHERE thread.id = :threadId " +
							"AND user.id = :userId "  +
							"AND participant.deleted = FALSE "  +
					"ORDER BY message.createdTimestamp DESC ";

			@SuppressWarnings("unchecked")
			Object[] queryResult = (Object[]) persistence.currentManager().createQuery(query)
					.setLong("threadId", threadId)
					.setLong("userId", userId)
					.setMaxResults(1)
					.uniqueResult();

			Result<MessageThreadData> result = new Result<>();

			if (queryResult != null) {
				MessageThread thread = (MessageThread) queryResult[0];
				ThreadParticipant participant = (ThreadParticipant) queryResult[1];
				Message lastMessage = (Message) queryResult[2];

				// mark thread as read by setting the last read message
				participant.setRead(true);
				participant.setLastReadMessage(lastMessage);
				persistence.currentManager().flush();


				Map<String, String> parameters = new HashMap<>();
				parameters.put("threadId", String.valueOf(threadId));

				result.appendEvent(eventFactory.generateEventData(EventType.READ_MESSAGE_THREAD,
						context, thread, null, null, parameters));

				result.setResult(new MessageThreadData(thread, userId));
			}

			return result;
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while marking thread as read");
		}
	}

	@Override
	@Transactional
	public List<MessageData> getAllUnreadMessages(long threadId, long userId) {
		String query =
				"SELECT DISTINCT message " +
				"FROM MessageThread thread " +
				"LEFT JOIN thread.messages message " +
				"LEFT JOIN thread.participants participant " +
				"WHERE thread.id = :threadId " +
					"AND participant.user.id = :userId " +
					"AND (participant.lastReadMessage IS NULL OR message.createdTimestamp > participant.lastReadMessage.createdTimestamp) " + // if lastReadMessage is null, user hasn't seen any messages in this thread
					"AND message.createdTimestamp >= participant.showMessagesFrom " +
				"ORDER BY message.createdTimestamp ASC";

		List<Message> result = persistence.currentManager().createQuery(query)
				.setLong("threadId", threadId)
				.setLong("userId", userId)
				.list();

		List<MessageData> unreadMessages = new LinkedList<>();

		for (Message message : result) {
			MessageData m = new MessageData(message, false);
			unreadMessages.add(m);
		}
		return unreadMessages;
	}

	@Override
	@Transactional
	public List<MessageData> getReadMessages(long threadId, long userId, int page, int limit) {
		String queryValue =
				"SELECT DISTINCT message " +
				"FROM MessageThread thread " +
				"LEFT JOIN thread.messages message " +
				"LEFT JOIN thread.participants participant " +
				"WHERE thread.id = :threadId " +
					"AND participant.user.id = :userId " +
					"AND (participant.lastReadMessage IS NULL OR message.createdTimestamp <= participant.lastReadMessage.createdTimestamp) " + // if lastReadMessage is null, user hasn't seen any messages in this thread
					"AND message.createdTimestamp >= participant.showMessagesFrom " +
				"ORDER BY message.createdTimestamp DESC";

		List<Message> result =  persistence.currentManager().createQuery(queryValue)
				.setLong("threadId", threadId)
				.setLong("userId", userId)
				.setFirstResult(page*limit)
				.setMaxResults(limit + 1)
				.list();

		List<MessageData> readMessages = new LinkedList<>();

		for (Message message : result) {
			MessageData m = new MessageData(message, true);
			readMessages.add(m);
		}
		return readMessages;
	}

	@Override
	@Transactional
	public void updateArchiveStatus(long threadId, long userId, boolean archiveStatus) {
		String updateQuery =
				"UPDATE ThreadParticipant " +
				"SET archived = :archive " +
				"WHERE messageThread.id = :threadId " +
						"AND user.id = :userId";

		persistence.currentManager().createQuery(updateQuery)
				.setLong("threadId", threadId)
				.setLong("userId", userId)
				.setBoolean("archive", archiveStatus)
				.executeUpdate();

		persistence.currentManager().flush();
	}

	@Override
	@Transactional
	public void markThreadDeleted(long threadId, long userId) {
		String updateQuery = "UPDATE ThreadParticipant SET deleted = true, showMessagesFrom = :timeFrom WHERE messageThread.id = :threadId AND user.id = :userId";
		Query query = persistence.currentManager().createQuery(updateQuery);
		query.setLong("threadId", threadId).setLong("userId", userId).setTimestamp("timeFrom", new Date());
		query.executeUpdate();

	}

	@Override
	@Transactional
	public boolean hasUserUnreadMessages(long userId) {
		String query =
				"SELECT participant.id " +
				"FROM ThreadParticipant participant " +
				"WHERE participant.user.id = :userId " +
					"AND participant.read = FALSE " +
					"AND participant.deleted = FALSE ";

		return persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setMaxResults(1)
				.uniqueResult() != null;
	}

	@Override
	@Transactional
	public MessageThreadData getMessageThread(long threadId, long userId) {
		try {
			MessageThread messageThread = loadResource(MessageThread.class, threadId);

			MessageThreadData messageThreadData = new MessageThreadData(messageThread, userId);

			return messageThreadData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading message thread");
		}
	}

	@Override
	@Transactional
	public MessageThread getMessageThreadForUsers(long loggedUserId, long otherUserId) {
		try {
			String query =
					"SELECT thread " +
					"FROM MessageThread thread " +
					"LEFT JOIN thread.participants participant1  " +
					"LEFT JOIN thread.participants participant2  " +
					"WHERE participant1.user.id = :userId1 " +
							"AND participant2.user.id = :userId2";

			return (MessageThread) persistence.currentManager().createQuery(query)
					.setLong("userId1", loggedUserId)
					.setLong("userId2", otherUserId)
					.setMaxResults(1)
					.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
			throw new DbConnectionException("Error loading message thread");
		}
	}

	@Override
	@Transactional
	public Optional<MessageThreadData> getMessageThreadDataForUsers(long loggedUserId, long otherUserId) {
		try {
			MessageThread thread = getMessageThreadForUsers(loggedUserId, otherUserId);

			if (thread != null) {
				return Optional.of(new MessageThreadData(thread, loggedUserId));
			}
			return Optional.empty();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
			throw new DbConnectionException("Error loading message thread");
		}
	}

	@Override
	@Transactional
	public long getSenderId(long messageId, Session session) {
		try {
			String query =
					"SELECT user.id " +
					"FROM Message message " +
					"LEFT JOIN message.sender sender " +
					"LEFT JOIN sender.user user " +
					"WHERE message.id = :messageId";

			return (Long) session.createQuery(query)
					.setLong("messageId", messageId)
					.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
			throw new DbConnectionException("Error loading message sender");
		}
	}

	@Override
	@Transactional
	public List<MessageThreadParticipantData> getThreadParticipantsForMessage(long messageId, Session session) {
		try {
			String query =
					"SELECT thread.participants " +
					"FROM Message message " +
					"LEFT JOIN message.messageThread thread " +
					"WHERE message.id = :messageId ";

			List<ThreadParticipant> participants = session.createQuery(query)
					.setLong("messageId", messageId)
					.list();

			List<MessageThreadParticipantData> participantDataList = new LinkedList<>();

			if (participants != null) {
				for (ThreadParticipant participant : participants) {
					participantDataList.add(new MessageThreadParticipantData(participant));
				}
			}

			return participantDataList;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
			throw new DbConnectionException("Error loading message sender");
		}
	}
}