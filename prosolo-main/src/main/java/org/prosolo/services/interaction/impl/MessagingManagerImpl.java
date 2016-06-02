package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.interaction.MessagingManager")
public class MessagingManagerImpl extends AbstractManagerImpl implements MessagingManager {

	private static final long serialVersionUID = -2828167274273122046L;

	private static Logger logger = Logger.getLogger(MessagingManagerImpl.class);

	@Autowired
	private EventFactory eventFactory;
	@Autowired
	private UserManager userManager;
	
	@Override
	@Transactional
	public Message sendMessage(long senderId, long receiverId, String msg) throws DbConnectionException {
		try {
			MessageThread messagesThread = findMessagesThreadForUsers(Arrays.asList(senderId,receiverId));

			User sender = new User();
			sender.setId(senderId);

			if (messagesThread == null) {
				List<Long> participantsIds = new ArrayList<Long>();
				participantsIds.add(receiverId);
				participantsIds.add(senderId);

				messagesThread = createNewMessagesThread(sender, participantsIds, msg);
			}

			Message message = sendSimpleOfflineMessage(sender, receiverId, msg, messagesThread, null);

			messagesThread = merge(messagesThread);
			messagesThread.addMessage(message);
			messagesThread.setLastUpdated(new Date());
			saveEntity(messagesThread);

			message.setMessageThread(messagesThread);
			saveEntity(message);

			return message;

		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while sending the message");
		}
	}
	
	private Message sendSimpleOfflineMessage(User sender, long receiverId, String content, MessageThread messagesThread,
			String context) throws ResourceCouldNotBeLoadedException {
		User receiver = loadResource(User.class, receiverId);
		return sendSimpleOfflineMessage(sender.getId(), receiver.getId(), content, messagesThread.getId(), context);
	}

	private Message sendSimpleOfflineMessage(long senderId, long receiverId, String content, long threadId,
			String context) throws ResourceCouldNotBeLoadedException {
		Date now = new Date();
		// User sender = loadResource(User.class, senderId);
		User receiver = loadResource(User.class, receiverId);
		MessageThread thread = loadResource(MessageThread.class, threadId);

		Message message = new Message();
		message.setCreatedTimestamp(now);
		ThreadParticipant msgSender = thread.getParticipant(senderId);
		ThreadParticipant msgReceiver = thread.getParticipant(receiverId);

		if (msgSender == null || msgReceiver == null) {
			throw new ResourceCouldNotBeLoadedException(String.format(
					"Either sending user : %s or recieving user : %s are not participents" + " of message thread : %s",
					senderId, receiverId, threadId));
		}
		//if msgReciever had this thread deleted, undelete it (it will have same effect as if we created new thread, as show_messages_from is set when deleting thread)
		msgReceiver.setDeleted(false);
		
		//As we are in transaction, changes will be reflected in DB
		msgSender.setRead(true);
		msgSender.setLastReadMessage(message);
		msgReceiver.setRead(false);
		msgReceiver.setUser(receiver);

		message.setSender(msgSender);

		message.setContent(content);
		message.setDateCreated(now);
		message.setMessageThread(thread);
		// save the message
		message = saveEntity(message);

		return message;
	}


	@Override
	public Message sendMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context)
			throws ResourceCouldNotBeLoadedException {
		Message message = createMessages(senderId, receivers, text, threadId, context);
		return message;
	}
	


	//Sending null for thread id does not mean that it doesn't exist, only that we don't know if it exists
	@Transactional
	public Message createMessages(long senderId, List<UserData> receivers, String text, Long threadId, String context)
			throws ResourceCouldNotBeLoadedException {
		Date now = new Date();
		
		MessageThread thread = null;
		//thread id is null, check if there is a thread with those participants
		if(threadId == null) {
			List<Long> participantIds = receivers.stream().map(UserData::getId).collect(Collectors.toList());
			participantIds.add(senderId);
			thread = findMessagesThreadForUsers(participantIds);
			//if thread is still null, then there is no thread for these participants, create one
			if(thread == null) {
				User sender = new User();
				sender.setId(senderId);
				thread = createNewMessagesThread(sender, participantIds, text);
			}
		}
		else {
			//we have the id, fetch the thread
			thread = findMessageThread(threadId);
		}

		Message message = new Message();
		message.setCreatedTimestamp(now);

		for (UserData receiverData : receivers) {
			if (receiverData.getId() == senderId) {
				ThreadParticipant msgSender = thread.getParticipant(senderId);
				msgSender.setRead(true);
				//un-archive message
				msgSender.setArchived(false);
				msgSender.setLastReadMessage(message);
				message.setSender(msgSender);
				continue;
			}
			ThreadParticipant msgReceiver = thread.getParticipant(receiverData.getId());
			msgReceiver.setRead(false);
			//un-archive message
			msgReceiver.setArchived(false);
			//if msgReciever had this thread deleted, undelete it (it will have same effect as if we created new thread, as show_messages_from is set when deleting thread)
			msgReceiver.setDeleted(false);
		}

		message.setContent(text);
		message.setDateCreated(now);
		message.setMessageThread(thread);


		thread.getMessages().add(message);
		thread.setLastUpdated(now);
		saveEntity(message);
		saveEntity(thread);
		return message;
	}

	@Override
	@Transactional(readOnly = false)
	public MessageThread createNewMessagesThread(User creator, List<Long> participantIds, String subject) {
		Date now = new Date();
		MessageThread messagesThread = new MessageThread();
		messagesThread.setCreator(creator);

		List<User> participants = userManager.loadUsers(participantIds);

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

		try {
			eventFactory.generateEvent(EventType.START_MESSAGE_THREAD, creator, messagesThread);
		} catch (EventException e) {
			logger.error(e);
		}
		return messagesThread;
	}

	@Transactional(readOnly = true)
	public MessageThread findMessagesThreadForUsers(List<Long> userIds) {
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
	public List<MessageThread> getLatestUserMessagesThreads(User user, int page, int limit, boolean archived) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participants " + "WHERE :userId IN (participants.user.id) "
				+" AND participants.archived = :archived "
				+" AND participants.deleted = false "
				+ "ORDER BY thread.lastUpdated DESC";

		Session session = this.persistence.openSession();
		List<MessageThread> result = null;

		try {
			result = session.createQuery(query).setLong("userId", user.getId()).setBoolean("archived", archived)
					.setFirstResult(page * limit)
					.setMaxResults(limit).list();

			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		if (result != null) {
			return result;
		} else {
			return new ArrayList<MessageThread>();
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
				+ "ORDER BY message.createdTimestamp ASC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query).setLong("threadId", threadId)
				.setTimestamp("fromTime", fromTime)
				.setFirstResult(offset).setMaxResults(limit + 1).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public MessageThread getLatestMessageThread(User user, boolean archived) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participants " + "WHERE :userId IN (participants.user.id) "
				+ "AND participants.archived = :archived "
				+ "AND participants.deleted = false "
				+ "ORDER BY thread.lastUpdated DESC ";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query)
				.setLong("userId", user.getId())
				.setBoolean("archived", archived)
				.setMaxResults(1).list();

		if (!result.isEmpty()) {
			return result.iterator().next();
		} else {
			return null;
		}
	}

	@Override
	public List<MessagesThreadData> convertMessagesThreadsToMessagesThreadData(List<MessageThread> mThreads,
			User user) {
		List<MessagesThreadData> messagesThread = new LinkedList<MessagesThreadData>();

		for (MessageThread mThread : mThreads) {
			MessagesThreadData mtData = convertMessagesThreadToMessagesThreadData(mThread, user);
			messagesThread.add(mtData);
		}
		return messagesThread;
	}

	@Override
	public MessagesThreadData convertMessagesThreadToMessagesThreadData(MessageThread mThread, User user) {
		Session session = this.persistence.openSession();
		MessagesThreadData mtData = new MessagesThreadData();

		try {
			mThread = (MessageThread) session.merge(mThread);
			mtData.setParticipantsList(createParticipantsList(mThread));
			mtData.setUpdateTime(DateUtil.createUpdateTime(mThread.getLastUpdated()));
			mtData.setLastUpdated(mThread.getLastUpdated());
			boolean isReaded = mThread.getParticipant(user.getId()).isRead();

			if (!isReaded) {
				System.out.println("Has unreaded messages:" + user.getId() + " " + user.getLastname() + " "
						+ mtData.getParticipantsList());
			}

			mtData.setReaded(isReaded);
			mtData.setSubject(mThread.getSubject());
			mtData.setId(mThread.getId());

			List<Message> messages = mThread.getMessages();

			List<MessageData> messagesData = new ArrayList<MessageData>();

			for (Message m : messages) {
				messagesData.add(new MessageData(m, user));
			}

			mtData.setMessages(messagesData);

			List<UserData> participantsWithoutLoggedUser = mThread.getParticipants().stream()
					 .map(tp -> UserDataFactory.createUserData(tp.getUser()))
					 .filter(ud -> ud.getId() != user.getId())
					 .sorted().collect(Collectors.toList());

			String participantsWithoutLoggedUserNames = participantsWithoutLoggedUser.stream()
													.map(UserData::getName)
													.collect(Collectors.joining(", "));

			mtData.setParticipantsListWithoutLoggedUser(participantsWithoutLoggedUserNames);
			mtData.setParticipantsWithoutLoggedUser(participantsWithoutLoggedUser);
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		return mtData;
	}

	private String createParticipantsList(MessageThread thread) {
		return thread.getParticipants().stream()
				.map(tp -> tp.getUser().getName() + " " + tp.getUser().getLastname())
			    .collect(Collectors.joining(", "));
	}

	
	public boolean markThreadAsRead(long threadId, long userId) {
		Session session = this.getPersistence().openSession();
		MessageThread thread = null;

		try {
			thread = (MessageThread) session.get(MessageThread.class, threadId);
			Optional<Message> latestMessage = thread.getMessages().stream().max(Comparator.comparing(Message::getCreatedTimestamp));

			if (thread != null) {
				ThreadParticipant participant = thread.getParticipant(userId);

				if (participant != null) {
					participant.setRead(true);
					latestMessage.ifPresent(msg -> participant.setLastReadMessage(msg));
					session.save(participant);
				}
			}

			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		return true;
	}


	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public ThreadParticipant findParticipation(long threadId, long userId) {
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
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Message> getUnreadMessages(long threadId, Message lastReadMessage, Date fromTime) {
		Query countQuery = createUnreadMessagesQuery(threadId, lastReadMessage, fromTime);
		return countQuery.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Message> getMessagesBeforeMessage(long threadId, Message message, int numberOfMessages, Date fromTime) {
		String queryValue = "SELECT DISTINCT message FROM MessageThread thread LEFT JOIN thread.messages message WHERE thread.id = :threadId";
		//if lastReadMessage is null, this user hasn't seen any messages in this thread (skip filtering by lastReadMessage id)
		if(message != null) {
			queryValue = queryValue + " AND message.createdTimestamp < :lastReadMessageTimestamp";
		}
		queryValue = queryValue + " AND message.createdTimestamp >= :fromTime ORDER BY message.createdTimestamp ASC";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId).setTimestamp("fromTime", fromTime);
		if(message != null) {
			query.setTimestamp("lastReadMessageTimestamp", message.getCreatedTimestamp());
		}
		return query.setMaxResults(numberOfMessages).list();
	}
	
	private Query createUnreadMessagesQuery(long threadId, Message lastReadMessage, Date fromTime) {
		String queryValue = "SELECT DISTINCT message FROM MessageThread thread LEFT JOIN thread.messages message WHERE thread.id = :threadId";
		//if lastReadMessage is null, this user hasn't seen any messages in this thread (skip filtering by lastReadMessage id)
		if(lastReadMessage != null) {
			queryValue = queryValue + " AND message.createdTimestamp > :lastReadMessageTimestamp";
		}
		queryValue = queryValue + " AND message.createdTimestamp >= :fromTime ORDER BY message.createdTimestamp ASC";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId).setTimestamp("fromTime", fromTime);
		if(lastReadMessage != null) {
			query.setTimestamp("lastReadMessageTimestamp", lastReadMessage.getCreatedTimestamp());
		}
		return query;
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
	public List<MessageThread> getUnreadMessageThreads(long userId) {
		//no need for archive flag, new messages always un-archive thread
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participants "
				+ "LEFT JOIN thread.messages messages "
				+ "WHERE participants.user.id = :userId "
				+ "AND messages.createdTimestamp >= participants.showMessagesFrom "
				+ "AND participants.deleted = false "
				+ "AND participants.read = false "
				+ "ORDER BY thread.lastUpdated DESC ";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.list();

		if (!result.isEmpty()) {
			return result;
		} else {
			return null;
		}
	
	}
	
	private MessageThread findMessageThread(long threadId) {
		String queryValue = "SELECT thread FROM MessageThread thread WHERE id = :threadId";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId);
		return (MessageThread) query.uniqueResult();
	}
	
	private Query createMultipleThreadparticipantsQuery(List<Long> userIds) {
		String queryString = 
				"SELECT DISTINCT thread " + 
				"FROM MessageThread thread " +
				"LEFT JOIN thread.participants participant " +
				"WHERE participant.id IN (:userIds)";
		
		return persistence.currentManager().createQuery(queryString).
				setParameterList("userIds", userIds);
//		StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT thread " + "FROM MessageThread thread ");
//		//create join clauses
//		for(int i = 0; i < userIds.size(); i++) {
//			queryBuilder.append(" LEFT JOIN thread.participants participant").append(i);
//		}
//		//create where clauses and named parameters
//		for(int i = 0; i < userIds.size(); i++) {
//			queryBuilder.append(" WHERE participant").append(i).append("user.id").append(" =: userid").append(i);
//			if(i < userIds.size()-1) {
//				queryBuilder.append(" AND ");
//			}
//		}
//		Query query = persistence.currentManager().createQuery(queryBuilder.toString());
//		//bind parameters
//		for(int i = 0; i < userIds.size(); i++) {
//			query.setLong("userid", userIds.get(i));
//		}
//		return query;
	}

}
