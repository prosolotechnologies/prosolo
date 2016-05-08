package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;

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
	@Transactional(readOnly = false)
	public Message sendSimpleOfflineMessage(User sender, long receiverId, String content, MessageThread messagesThread,
			String context) throws ResourceCouldNotBeLoadedException {
		User receiver = loadResource(User.class, receiverId);
		return sendSimpleOfflineMessage(sender.getId(), receiver.getId(), content, messagesThread.getId(), context);
	}

	@Override
	@Transactional(readOnly = false)
	public Message sendSimpleOfflineMessage(long senderId, long receiverId, String content, long threadId,
			String context) throws ResourceCouldNotBeLoadedException {

		// User sender = loadResource(User.class, senderId);
		User receiver = loadResource(User.class, receiverId);
		MessageThread thread = loadResource(MessageThread.class, threadId);

		Message message = new Message();
		message.setCreatedTimestamp(new Date());
		ThreadParticipant msgSender = thread.getParticipant(senderId);
		ThreadParticipant msgReceiver = thread.getParticipant(receiverId);

		if (msgSender == null || msgReceiver == null) {
			throw new ResourceCouldNotBeLoadedException(String.format(
					"Either sending user : %s or recieving user : %s are not participents" + " of message thread : %s",
					senderId, receiverId, threadId));
		}
		
		//As we are in transaction, changes will be reflected in DB
		msgSender.setRead(true);
		msgSender.setLastReadMessage(message);
		msgReceiver.setRead(false);
		msgReceiver.setUser(receiver);

		message.setSender(msgSender);

		message.setContent(content);
		message.setDateCreated(new Date());
		message.setMessageThread(thread);
		// save the message
		message = saveEntity(message);

		return message;
	}

	@Override
	@Transactional(readOnly = false)
	public Message sendMessages(long senderId, List<UserData> receivers, String text, long threadId, String context)
			throws ResourceCouldNotBeLoadedException {
		Message message = createMessages(senderId, receivers, text, threadId, context);

		getPersistence().flush();

		// for (ThreadParticipant mp : message.getParticipants()) {
		// if(!mp.isSender()) {
		// try {
		// Map<String, String> parameters = new HashMap<String, String>();
		// parameters.put("context", context);
		// parameters.put("user", String.valueOf(mp.getParticipant().getId()));
		// parameters.put("message", String.valueOf(message.getId()));
		// eventFactory.generateEvent(EventType.SEND_MESSAGE,
		// message.getSender().getParticipant(), message, parameters);
		// } catch (EventException e) {
		// logger.error(e);
		// }
		// }
		// }

		return message;
	}

	/*
	 * Executed in a separate session so events in MessageObserver can properly
	 * load these messages.
	 */
	/*
	 * @Transactional(propagation = Propagation.REQUIRES_NEW) public
	 * List<SimpleOfflineMessage> createMessages(long senderId, List<UserData>
	 * receivers, String text, long threadId, String context) throws
	 * ResourceCouldNotBeLoadedException { User sender =
	 * loadResource(User.class, senderId); MessagesThread thread =
	 * loadResource(MessagesThread.class, threadId);
	 * 
	 * List<SimpleOfflineMessage> sentMessages = new
	 * ArrayList<SimpleOfflineMessage>();
	 * 
	 * for (UserData receiverData : receivers) { if (receiverData.getId() ==
	 * senderId) { continue; }
	 * 
	 * User receiver = loadResource(User.class, receiverData.getId());
	 * 
	 * SimpleOfflineMessage message = new SimpleOfflineMessage();
	 * message.setSender(sender); message.setReceiver(receiver);
	 * message.setContent(text); message.setDateCreated(new Date());
	 * message.setRead(false); message.setMessageThread(thread); message =
	 * saveEntity(message);
	 * 
	 * sentMessages.add(message); }
	 * 
	 * thread.getMessages().addAll(sentMessages); thread.setLastUpdated(new
	 * Date()); saveEntity(thread);
	 * 
	 * return sentMessages; }
	 */

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Message createMessages(long senderId, List<UserData> receivers, String text, long threadId, String context)
			throws ResourceCouldNotBeLoadedException {

		User sender = loadResource(User.class, senderId);
		MessageThread thread = loadResource(MessageThread.class, threadId);

		Message message = new Message();

		Set<ThreadParticipant> participants = new HashSet<>();
		ThreadParticipant msgSender = new ThreadParticipant();
		msgSender.setRead(false);
		msgSender.setUser(sender);

		participants.add(msgSender);
		message.setSender(msgSender);

		for (UserData receiverData : receivers) {
			if (receiverData.getId() == senderId) {
				continue;
			}
			User receiver = loadResource(User.class, receiverData.getId());
			ThreadParticipant msgReceiver = new ThreadParticipant();
			msgReceiver.setRead(false);
			msgReceiver.setUser(receiver);

			participants.add(msgReceiver);
		}

		message.setContent(text);
		message.setDateCreated(new Date());
		message.setMessageThread(thread);

		message = saveEntity(message);

		thread.getMessages().add(message);
		thread.setLastUpdated(new Date());
		saveEntity(thread);

		return message;
	}

	@Override
	@Transactional(readOnly = false)
	public MessageThread createNewMessagesThread(User creator, List<Long> participantIds, String subject) {

		MessageThread messagesThread = new MessageThread();
		messagesThread.setCreator(creator);

		List<User> participants = userManager.loadUsers(participantIds);

		for (User user : participants) {
			ThreadParticipant participant = new ThreadParticipant();
			participant.setUser(user);
			messagesThread.addParticipant(participant);
			participant.setMessageThread(messagesThread);
		}

		Date now = new Date();
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

	@Override
	@Transactional(readOnly = true)
	public MessageThread findMessagesThreadForUsers(long user1Id, long user2Id) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participant1 " + "LEFT JOIN thread.participants participant2 "
				+ "WHERE participant1.user.id = :userid1 " + "AND participant2.user.id = :userid2";

		Session session = this.persistence.openSession();
		MessageThread messagesThread = null;

		try {
			messagesThread = (MessageThread) session.createQuery(query).setLong("userid1", user1Id)
					.setLong("userid2", user2Id).uniqueResult();

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

//	@SuppressWarnings("unchecked")
//	@Override
//	@Transactional(readOnly = true)
//	public List<Message> getUnreadSimpleOfflineMessages(User user, int page, int limit) {
//		String query = "SELECT DISTINCT message " + "FROM Message message " 
//				+ "LEFT JOIN MessageThread messageThread"
//				+ "LEFT JOIN ThreadParticipant participant"
//				+ "WHERE messageThread.id = message.messageThread.id"
//				+ "AND messageThread.id = participent.messageThread.id"
//				+ "AND message.id > participent.lastReadMessage.id "
//				+ "AND participant.user.id = :userId"
//				+ "AND participant.read = false"
//				+ "ORDER BY message.dateCreated DESC";
//
//		Session session = (Session) persistence.openSession();
//		List<Message> result = null;
//
//		try {
//			result = session.createQuery(query).setLong("userId", user.getId())
//					.setFirstResult(page * limit).setMaxResults(limit).list();
//
//			if (result != null) {
//				return result;
//			}
//			session.flush();
//		} catch (Exception e) {
//			logger.error("Exception in handling message", e);
//		} finally {
//			HibernateUtil.close(session);
//		}
//		return new ArrayList<Message>();
//	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getMessages(User user, int page, int limit) {
		String query = "SELECT DISTINCT message " + "FROM Message as message " + "LEFT JOIN ThreadParticipant participant "
				+ "WHERE participant.user.id = :userId " + "ORDER BY message.dateCreated DESC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query).setLong("userId", user.getId())
				.setFirstResult(page * limit).setMaxResults(limit).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getMessagesForThread(long threadId, int offset, int limit) {
		String query = "SELECT DISTINCT message " + "FROM MessageThread thread " + "LEFT JOIN thread.messages message "
				+ "WHERE thread.id = :threadId " + "ORDER BY message.dateCreated ASC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query).setLong("threadId", threadId)
				.setFirstResult(offset).setMaxResults(limit + 1).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getMessagesForThread(MessageThread thread, int page, int limit, Session session) {
		String query = "SELECT DISTINCT message " + "FROM MessageThread thread " + "LEFT JOIN thread.messages message "
				+ "WHERE thread = :thread " + "ORDER BY message.dateCreated ASC";

		@SuppressWarnings("unchecked")
		List<Message> result = session.createQuery(query).setEntity("thread", thread).setFirstResult(page * limit)
				.setMaxResults(limit).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MessageThread> getUserMessagesThreads(User user, int page, int limit) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participant " + "WHERE participant.user.id = :userId "
				+ "ORDER BY thread.lastUpdated DESC";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query).setLong("userId", user.getId())
				.setFirstResult(page * limit).setMaxResults(limit).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<MessageThread>();
	}

	@Override
	@Transactional(readOnly = true)
	public MessageThread getMessagesThreadForPrevoiusMessage(Message previousMessage) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread " + "LEFT JOIN thread.messages message "
				+ "WHERE message = :message";

		return (MessageThread) persistence.currentManager().createQuery(query).setEntity("message", previousMessage)
				.uniqueResult();
	}

	@Override
	@Transactional(readOnly = true)
	public MessageThread getLatestMessageThread(User user) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participants " + "WHERE :userId IN (participants.user.id) "
				+ "ORDER BY thread.lastUpdated DESC ";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query).setLong("userId", user.getId())
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

	@Override
	@Transactional(readOnly = false)
	public Message markAsRead(Message message, User user, Session session) {
		if (message != null) {
			for (ThreadParticipant mp : message.getMessageThread().getParticipants()) {
				if (mp.getUser().equals(user)) {
					if (!mp.isRead()) {
						mp.setRead(true);
					}
				}
			}
			session.saveOrUpdate(message);
			return message;
		}
		return null;
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

	@Override
	@Transactional
	public Message sendMessage(long senderId, long receiverId, String msg) throws DbConnectionException {
		try {
			MessageThread messagesThread = findMessagesThreadForUsers(senderId, receiverId);

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

//	@Override
//	@Transactional
//	public List<Message> getReadAndUnreadMessages(ThreadParticipant participant, int page, int limit) {
//		long threadId = participant.getMessageThread().getId();
//		Message lastReadMessage = participant.getLastReadMessage();
//		//TODO reduce number of queries by joining
//		List<Message> messagesToReturn = getUnreadMessages(threadId, lastReadMessage);
//		//if number of unread messages >= limit, pull 2 already read ones and join them with new ones
//		if (messagesToReturn.size() >= limit) {
//			List<Message> oldMessages = getMessagesBeforeMessage(threadId,lastReadMessage,2);
//			messagesToReturn.addAll(oldMessages);
//		}
//		else {
//			//shift standard pagination for the number of unread messages (first result must be "higher" for that number, last result must be "lower")
//			List<Message> oldMessages = getMessagesForThread(threadId,(page*limit) + messagesToReturn.size() ,limit - messagesToReturn.size());
//			messagesToReturn.addAll(oldMessages);
//		}
//		return messagesToReturn;
//	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Message> getUnreadMessages(long threadId, Message lastReadMessage) {
		Query countQuery = createUnreadMessagesQuery(threadId, lastReadMessage);
		return countQuery.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Message> getMessagesBeforeMessage(long threadId, Message message, int numberOfMessages) {
		String queryValue = "SELECT DISTINCT message FROM MessageThread thread LEFT JOIN thread.messages message WHERE thread.id = :threadId";
		//if lastReadMessage is null, this user hasn't seen any messages in this thread (skip filtering by lastReadMessage id)
		if(message != null) {
			queryValue = queryValue + " AND message.createdTimestamp < :lastReadMessageTimestamp";
		}
		queryValue = queryValue + " ORDER BY message.createdTimestamp ASC";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId);
		if(message != null) {
			query.setTimestamp("lastReadMessageTimestamp", message.getCreatedTimestamp());
		}
		return query.setMaxResults(numberOfMessages).list();
	}
	
	private Query createUnreadMessagesQuery(long threadId, Message lastReadMessage) {
		String queryValue = "SELECT DISTINCT message FROM MessageThread thread LEFT JOIN thread.messages message WHERE thread.id = :threadId";
		//if lastReadMessage is null, this user hasn't seen any messages in this thread (skip filtering by lastReadMessage id)
		if(lastReadMessage != null) {
			queryValue = queryValue + " AND message.createdTimestamp > :lastReadMessageTimestamp";
		}
		queryValue = queryValue + " ORDER BY message.createdTimestamp ASC";
		Query query = persistence.currentManager().createQuery(queryValue);
		query.setLong("threadId", threadId);
		if(lastReadMessage != null) {
			query.setTimestamp("lastReadMessageTimestamp", lastReadMessage.getCreatedTimestamp());
		}
		return query;
	}

}
