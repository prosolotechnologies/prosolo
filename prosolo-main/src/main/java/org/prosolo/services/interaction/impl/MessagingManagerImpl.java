package org.prosolo.services.interaction.impl;


import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("org.prosolo.services.interaction.MessagingManager")
public class MessagingManagerImpl extends AbstractManagerImpl implements MessagingManager {

	private static final long serialVersionUID = -2828167274273122046L;

	private static Logger logger = Logger.getLogger(MessagingManagerImpl.class);

	@Autowired
	private EventFactory eventFactory;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MessagingManager self;

//	@Transactional
//	public Message sendMessage1(long senderId, long receiverId, String msg) throws DbConnectionException {
//		try {
//			MessageThread messagesThread = getMessageThreadForUsers(Arrays.asList(senderId, receiverId));
//
//			if (messagesThread == null) {
//				List<Long> participantsIds = new ArrayList<Long>();
//				participantsIds.add(receiverId);
//				participantsIds.add(senderId);
//				messagesThread = createNewMessageThread(senderId, participantsIds, msg);
//			} else {
//				//check if thread was deleted by sender
//				ThreadParticipant participant = messagesThread.getParticipant(senderId);
//				if (participant.isDeleted()) {
//					participant.setDeleted(false);
//					persistence.merge(participant);
//				}
//			}
//
//			Message message = sendSimpleOfflineMessage(senderId, receiverId, msg, messagesThread.getId(), null);
//
//			messagesThread.addMessage(message);
//			messagesThread.setLastUpdated(new Date());
//			saveEntity(messagesThread);
//
//			message.setMessageThread(messagesThread);
//			saveEntity(message);
//
//			return message;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new DbConnectionException("Error while sending the message");
//		}
//	}

	@Override
	public MessageData sendMessage(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException {
		Result<Object[]> result = self.sendMessageAndGetEvents(threadId, senderId, receiverId, msg, contextData);
		eventFactory.generateEvents(result.getEventQueue());
		return (MessageData) result.getResult()[0];
	}


	@Override
	public Object[] sendMessageAndReturnMessageAndThread(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException {
		Result<Object[]> result = self.sendMessageAndGetEvents(threadId, senderId, receiverId, msg, contextData);
		eventFactory.generateEvents(result.getEventQueue());
		return result.getResult();
	}

	@Override
	@Transactional
	public Result<Object[]> sendMessageAndGetEvents(long threadId, long senderId, long receiverId, String msg, UserContextData contextData)
			throws DbConnectionException {
		try {
			Result<Object[]> result = new Result<>();

			MessageThread thread ;
			Date now = new Date();

			if (threadId > 0) {
				thread = loadResource(MessageThread.class, threadId);
			} else {
				thread = getMessageThreadForUsers(Arrays.asList(senderId, receiverId));

				if (thread == null) {
					Result<MessageThread> threadResult = createNewMessageThread(senderId, Arrays.asList(senderId, receiverId), msg);
					thread = threadResult.getResult();
					result.appendEvents(threadResult.getEventQueue());
				}
			}

			Message message = new Message();
			message.setCreatedTimestamp(now);
			message = saveEntity(message);

			ThreadParticipant msgSender = thread.getParticipant(senderId);
			msgSender.setRead(true);
			msgSender.setLastReadMessage(message);
			//check if thread was deleted by sender
			if (msgSender.isDeleted()) {
				msgSender.setDeleted(false);
				msgSender.setShowMessagesFrom(now);
			}
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

//			Map<String, String> parameters = new HashMap<>();
//			parameters.put("threadId", String.valueOf(messagesThreadData.getId()));
//
//			result.appendEvent(eventFactory.generateEventData(EventType.READ_MESSAGE_THREAD,
//					contextData, message.getMessageThread(), null, null, parameters));


			Map<String, String> parameters = new HashMap<String, String>();

			parameters.put("context", contextData.getContext().getLearningContext());
			parameters.put("users", thread.getParticipants().stream().map(u ->
					String.valueOf(u.getId())).collect(Collectors.joining(",")));
			parameters.put("user", String.valueOf(thread.getParticipants().iterator().next().getId()));
			parameters.put("message", String.valueOf(message.getId()));

			result.appendEvent(eventFactory.generateEventData(EventType.SEND_MESSAGE, contextData,
					message, null, null, parameters));

			result.setResult(new Object[]{new MessageData(message, true), new MessagesThreadData(thread, senderId)});

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while sending the message");
		}
	}

//	private Message sendSimpleOfflineMessage(long senderId, long receiverId, String content, long threadId,
//											 String context) throws ResourceCouldNotBeLoadedException {
//		Date now = new Date();
//		User receiver = loadResource(User.class, receiverId);
//		MessageThread thread = loadResource(MessageThread.class, threadId);
//
//		Message message = new Message();
//		message.setCreatedTimestamp(now);
//		ThreadParticipant msgSender = thread.getParticipant(senderId);
//		ThreadParticipant msgReceiver = thread.getParticipant(receiverId);
//
//		if (msgSender == null || msgReceiver == null) {
//			throw new ResourceCouldNotBeLoadedException(String.format(
//					"Either sending user : %s or recieving user : %s are not participents" + " of message thread : %s",
//					senderId, receiverId, threadId));
//		}
//		//if msgReciever had this thread deleted, undelete it (it will have same effect as if we created new thread, as show_messages_from is set when deleting thread)
//		msgReceiver.setDeleted(false);
//
//		//As we are in transaction, changes will be reflected in DB
//		msgSender.setRead(true);
//		msgSender.setLastReadMessage(message);
//		msgReceiver.setRead(false);
//		msgReceiver.setUser(receiver);
//
//		message.setSender(msgSender);
//
//		message.setContent(content);
//		message.setDateCreated(now);
//		message.setMessageThread(thread);
//		// save the message
//		message = saveEntity(message);
//
//		return message;
//	}
//
//
//	@Override
//	public void sendMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context, UserContextData contextData)
//			throws ResourceCouldNotBeLoadedException {
//
//		Result<Void> result = self.sendMessagesAndGetEvents(senderId, receivers, text, threadId, context, contextData);
//		eventFactory.generateEvents(result.getEventQueue());
//	}
//
//	@Override
//	public Result<Void> sendMessagesAndGetEvents(long senderId, List<UserData> receivers, String text, Long threadId, String context, UserContextData contextData)
//			throws ResourceCouldNotBeLoadedException {
//
//		Message message = createMessages(senderId, receivers, text, threadId, context);
//		Result<Void> result = new Result<>();
//		Map<String, String> parameters = new HashMap<String, String>();
//
//		parameters.put("context", contextData.getContext().getLearningContext());
//		parameters.put("users", receivers.stream().map(u ->
//				String.valueOf(u.getId())).collect(Collectors.joining(",")));
//		parameters.put("user", String.valueOf(receivers.get(0).getId()));
//		parameters.put("message", String.valueOf(message.getId()));
//
//		result.appendEvent(eventFactory.generateEventData(EventType.SEND_MESSAGE, contextData,
//				message, null, null, parameters));
//
//		return result;
//	}


//	//Sending null for thread id does not mean that it doesn't exist, only that we don't know if it exists
//	@Transactional
//	public Message createMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context)
//			throws ResourceCouldNotBeLoadedException {
//		Date now = new Date();
//
//		MessageThread thread = null;
//
//		//thread id is null, check if there is a thread with those participants
//		if (threadId == null) {
//			List<Long> participantIds = receivers.stream().map(UserData::getId).collect(Collectors.toList());
//			participantIds.add(senderId);
//			thread = getMessageThreadForUsers(participantIds);
//			//if thread is still null, then there is no thread for these participants, create one
//		}
//		else {
//			//we have the id, fetch the thread
//			thread = findMessageThread(threadId);
//		}
//
//		Message message = new Message();
//		message.setCreatedTimestamp(now);
//
//		for (UserData receiverData : receivers) {
//			if (receiverData.getId() == senderId) {
//				ThreadParticipant msgSender = thread.getParticipant(senderId);
//				msgSender.setRead(true);
//				//un-archive message
//				msgSender.setArchived(false);
//				msgSender.setLastReadMessage(message);
//				message.setSender(msgSender);
//				continue;
//			}
//			ThreadParticipant msgReceiver = thread.getParticipant(receiverData.getId());
//			msgReceiver.setRead(false);
//			//un-archive message
//			msgReceiver.setArchived(false);
//			//if msgReciever had this thread deleted, undelete it (it will have same effect as if we created new thread, as show_messages_from is set when deleting thread)
//			msgReceiver.setDeleted(false);
//		}
//
//		message.setContent(text);
//		message.setDateCreated(now);
//		message.setMessageThread(thread);
//
//
//		thread.getMessages().add(message);
//		thread.setLastUpdated(now);
//		saveEntity(message);
//		saveEntity(thread);
//		return message;
//	}

	@Override
	@Transactional
	public Result<MessageThread> createNewMessageThread(long creatorId, List<Long> participantIds, String subject) throws ResourceCouldNotBeLoadedException {
		Date now = new Date();
		MessageThread messagesThread = new MessageThread();
		messagesThread.setCreator(loadResource(User.class, creatorId));

		List<User> participants = userManager.loadUsers(participantIds);
		if(participants.size() < participantIds.size()) {
			//some of the user ids do not exist
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

	@Transactional(readOnly = true)
	public MessageThread getMessageThreadForUsers(List<Long> userIds) {
		Query query = createMultipleThreadparticipantsQuery(userIds);

		Session session = this.persistence.openSession();
		MessageThread messagesThread = null;

		try {
			messagesThread = (MessageThread)query.uniqueResult();

			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		return messagesThread;
	}


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<MessagesThreadData> getMessageThreads(long userId, int page, int limit, boolean archived) {
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
						.setMaxResults(limit);
			}

			List<MessageThread> result = q.list();

			List<MessagesThreadData> messageThreadData = new LinkedList<>();

			if (result != null) {
				for (MessageThread mThread : result) {
					MessagesThreadData mtData = new MessagesThreadData(mThread, userId);
					messageThreadData.add(mtData);
				}
			}
			return messageThreadData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading message threads");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getMessagesForThread(long threadId, int offset, int limit, Date fromTime) {
		String query = "SELECT DISTINCT message "
				+ "FROM MessageThread thread "
				+ "LEFT JOIN thread.messages message "
				+ "WHERE thread.id = :threadId "
				+ "AND message.createdTimestamp >= :fromTime "
				+ "ORDER BY message.createdTimestamp DESC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query).setLong("threadId", threadId)
				.setTimestamp("fromTime", fromTime)
				.setFirstResult(offset).setMaxResults(limit + 1).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

//	@Override
//	public MessageThread getLatestMessageThread(long userId, boolean archived, UserContextData context) {
//		Result<MessageThread> result = self.getLatestMessageThreadAndGetEvents(userId, archived, context);
//
//		eventFactory.generateEvents(result.getEventQueue());
//
//		return result.getResult();
//	}

//	@Override
//	@Transactional
//	public Result<MessageThread> getLatestMessageThreadAndGetEvents(long userId, boolean archived, UserContextData context)
//			throws DbConnectionException {
//		String query =
//				"SELECT DISTINCT thread " +
//				"FROM MessageThread thread " +
//				"LEFT JOIN thread.participants participants " +
//				"WHERE :userId IN (participants.user.id) "  +
//					"AND participants.archived = :archived " +
//					"AND participants.deleted = false " +
//				"ORDER BY thread.lastUpdated DESC ";
//
//		List<MessageThread> result = persistence.currentManager().createQuery(query)
//				.setLong("userId", userId)
//				.setBoolean("archived", archived)
//				.setMaxResults(1).list();
//
//		Result<MessageThread> res = new Result<>();
//
//		if (!result.isEmpty()) {
//			MessageThread messageThread = result.iterator().next();
//			MessagesThreadData messagesThreadData = new MessagesThreadData(messageThread, userId);
//
//			Map<String, String> parameters = new HashMap<>();
//			parameters.put("threadId", String.valueOf(messageThread.getId()));
//
//			res.appendEvent(eventFactory.generateEventData(EventType.READ_MESSAGE_THREAD,
//					context, messageThread, null, null, parameters));
//
//			res.setResult(messageThread);
//		} else {
//			return null;
//		}
//
//		return res;
//	}

//	@Override
//	public String sendMessageDialog(long senderId, long receiverId, String msg, UserContextData contextData)
//			throws DbConnectionException {
//		Result<String> result = self.sendMessageDialogAndGetEvents(senderId, receiverId, msg, contextData);
//		eventFactory.generateEvents(result.getEventQueue());
//		return result.getResult();
//	}
//
//	@Override
//	@Transactional
//	public Result<String> sendMessageDialogAndGetEvents(long senderId, long receiverId, String msg, UserContextData contextData)
//			throws DbConnectionException {
//		try {
//			MessageThread messagesThread = getMessageThreadForUsers(Arrays.asList(senderId, receiverId));
//
//			if (messagesThread == null) {
//				List<Long> participantsIds = new ArrayList<Long>();
//				participantsIds.add(receiverId);
//				participantsIds.add(senderId);
//				messagesThread = createNewMessageThread(senderId, participantsIds, msg);
//			} else {
//				//check if thread was deleted by sender
//				ThreadParticipant participant = messagesThread.getParticipant(senderId);
//				if (participant.isDeleted()) {
//					participant.setDeleted(false);
//					persistence.merge(participant);
//				}
//			}
//
//			Message message = sendSimpleOfflineMessage(senderId, receiverId, msg, messagesThread.getId(), null);
//
//			messagesThread.addMessage(message);
//			messagesThread.setLastUpdated(new Date());
//			saveEntity(messagesThread);
//
//			message.setMessageThread(messagesThread);
//			saveEntity(message);
//
//			Result<String> result = new Result<>();
//			Map<String, String> parameters = new HashMap<String, String>();
//			parameters.put("user", String.valueOf(receiverId));
//			parameters.put("message", String.valueOf(message.getId()));
//
//			result.appendEvent(eventFactory.generateEventData(EventType.SEND_MESSAGE, contextData,
//					message, null,null, parameters));
//
//			result.setResult(message.getContent());
//
//			return result;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new DbConnectionException("Error while sending the message");
//		}
//	}

	@Override
	// nt
	public MessagesThreadData markThreadAsRead(long threadId, long userId, UserContextData context) throws DbConnectionException {
		Result<MessagesThreadData> res = self.markThreadAsReadAndGetEvents(threadId, userId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<MessagesThreadData> markThreadAsReadAndGetEvents(long threadId, long userId, UserContextData context) throws DbConnectionException {
		try {
			String query =
					"SELECT message " +
					"FROM MessageThread thread " +
					"LEFT JOIN thread.messages message " +
					"WHERE thread.id = :threadId "  +
					"ORDER BY message.createdTimestamp DESC ";

			@SuppressWarnings("unchecked")
			Message lastMessage = (Message) persistence.currentManager().createQuery(query)
					.setLong("threadId", threadId)
					.setMaxResults(1).uniqueResult();

			// since Hibernate does not support JOIN in the UPDATE query, we must first obtain participantId
			String query1 =
					"SELECT participant.id " +
					"FROM ThreadParticipant participant " +
					"WHERE participant.user.id = :userId " +
						"AND participant.messageThread.id = :threadId";

			@SuppressWarnings("unchecked")
			Long participantId = (Long) persistence.currentManager().createQuery(query1)
					.setLong("threadId", threadId)
					.setLong("userId", userId)
					.uniqueResult();

			// update the thread participant
			String query2 =
					"UPDATE ThreadParticipant participant SET " +
					"participant.read = TRUE, " +
					"participant.lastReadMessage = :lastMessage " +
					"WHERE participant.id = :participantId";

			persistence.currentManager()
					.createQuery(query2)
					.setEntity("lastMessage", lastMessage)
					.setLong("participantId", participantId)
					.executeUpdate();


			Map<String, String> parameters = new HashMap<>();
			parameters.put("threadId", String.valueOf(threadId));

			Result<MessagesThreadData> result = new Result<>();

			MessageThread thread = (MessageThread) persistence.currentManager().get(MessageThread.class, threadId);

			result.appendEvent(eventFactory.generateEventData(EventType.READ_MESSAGE_THREAD,
					context, thread, null, null, parameters));

			result.setResult(new MessagesThreadData(thread, userId));

			return result;
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while marking thread as read");
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public ThreadParticipant getThreadParticipant(long threadId, long userId) {
		String query = "SELECT threadParticipant " + "FROM ThreadParticipant threadParticipant "
				+  "WHERE threadParticipant.user.id = :userId AND threadParticipant.messageThread.id = :threadId";

		@SuppressWarnings("unchecked")
		List<ThreadParticipant> result = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.setLong("threadId", threadId)
				.setMaxResults(1).list();

		if (!result.isEmpty()) {
			return result.iterator().next();
		} else {
			return null;
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
	public void archiveThread(long threadId, long userId) {
		String updateQuery = "UPDATE ThreadParticipant SET archived=true WHERE messageThread.id = :threadId AND user.id = :userId";
		Query query = persistence.currentManager().createQuery(updateQuery);
		query.setLong("threadId", threadId).setLong("userId", userId);
		query.executeUpdate();

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
	public boolean userHasUnreadMessages(long userId) {
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
	public MessagesThreadData getMessageThread(long threadId, long userId) {
		try {
			MessageThread messageThread = loadResource(MessageThread.class, threadId);

			MessagesThreadData messagesThreadData = new MessagesThreadData(messageThread, userId);

			return messagesThreadData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error loading message thread");
		}
	}

	private MessageThread findMessageThread(long threadId) {
		String queryValue = "SELECT thread FROM MessageThread thread WHERE id = :threadId";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId);
		return (MessageThread) query.uniqueResult();
	}

	private Query createMultipleThreadparticipantsQuery(List<Long> userIds) {
		StringBuilder queryBuilder = new StringBuilder(
				"SELECT DISTINCT thread " +
				"FROM MessageThread thread ");

		//create join clauses
		for(int i = 0; i < userIds.size(); i++) {
			queryBuilder.append(" LEFT JOIN thread.participants participant").append(i);
		}

		//create where clauses and named parameters
		for (int i = 0; i < userIds.size(); i++) {
			if (i == 0) {
				queryBuilder.append(" WHERE ");
			}
			queryBuilder.append("participant" + i + ".user.id =:userid" + i);
			if (i < userIds.size() - 1) {
				queryBuilder.append(" AND ");
			}
		}
		Query query = persistence.currentManager().createQuery(queryBuilder.toString());
		//bind parameters
		for(int i = 0; i < userIds.size(); i++) {
			query.setLong("userid"+i, userIds.get(i));
		}
		return query;
	}

}