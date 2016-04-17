package org.prosolo.services.interaction.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageParticipant;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.communications.data.MessageData;
import org.prosolo.web.communications.data.MessagesThreadData;
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
	public Message sendSimpleOfflineMessage(User sender, long receiverId, String content,
			MessageThread messagesThread, String context) throws ResourceCouldNotBeLoadedException {
		User receiver = loadResource(User.class, receiverId);
		return sendSimpleOfflineMessage(sender.getId(), receiver.getId(), content, messagesThread.getId(), context);
	}

	@Override
	@Transactional(readOnly = false)
	public Message sendSimpleOfflineMessage(long senderId, long receiverId, String content, long threadId,
			String context) throws ResourceCouldNotBeLoadedException {

		User sender = loadResource(User.class, senderId);
		User receiver = loadResource(User.class, receiverId);
		MessageThread thread = loadResource(MessageThread.class, threadId);

		Message message = new Message();
		Set<MessageParticipant> participants = new HashSet<>();
		MessageParticipant msgSender = new MessageParticipant();
		msgSender.setRead(false);
		msgSender.setSender(true);
		msgSender.setParticipant(sender);
		
		participants.add(msgSender);
		message.setSender(msgSender);
		
		MessageParticipant msgReceiver = new MessageParticipant();
		msgReceiver.setRead(false);
		msgReceiver.setSender(false);
		msgReceiver.setParticipant(receiver);
		
		participants.add(msgReceiver);
		
		message.setParticipants(participants);
		
		message.setContent(content);
		message.setDateCreated(new Date());
		message.setMessageThread(thread);
		message = saveEntity(message);

		return message;
	}

//	@Override
//	@Transactional(readOnly = false)
//	public List<SimpleOfflineMessage> sendMessages(long senderId, List<UserData> receivers, String text, long threadId,
//			String context) throws ResourceCouldNotBeLoadedException {
//		List<SimpleOfflineMessage> sentMessages = createMessages(senderId, receivers, text, threadId, context);
//
//		getPersistence().flush();
//
//		for (SimpleOfflineMessage message : sentMessages) {
//			try {
//				Map<String, String> parameters = new HashMap<String, String>();
//				parameters.put("context", context);
//				parameters.put("user", String.valueOf(message.getReceiver().getId()));
//				parameters.put("message", String.valueOf(message.getId()));
//				eventFactory.generateEvent(EventType.SEND_MESSAGE, message.getSender(), message, parameters);
//			} catch (EventException e) {
//				logger.error(e);
//			}
//		}
//
//		return sentMessages;
//	}
	
	@Override
	@Transactional(readOnly = false)
	public Message sendMessages(long senderId, List<UserData> receivers, String text, long threadId,
			String context) throws ResourceCouldNotBeLoadedException {
		Message message = createMessages(senderId, receivers, text, threadId, context);

		getPersistence().flush();

		for (MessageParticipant mp : message.getParticipants()) {
			if(!mp.isSender()) {
				try {
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("context", context);
					parameters.put("user", String.valueOf(mp.getParticipant().getId()));
					parameters.put("message", String.valueOf(message.getId()));
					eventFactory.generateEvent(EventType.SEND_MESSAGE, message.getSender().getParticipant(), message, parameters);
				} catch (EventException e) {
					logger.error(e);
				}
			}
		}

		return message;
	}

	/*
	 * Executed in a separate session so events in MessageObserver can properly
	 * load these messages.
	 */
	/*@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<SimpleOfflineMessage> createMessages(long senderId, List<UserData> receivers, String text,
			long threadId, String context) throws ResourceCouldNotBeLoadedException {
		User sender = loadResource(User.class, senderId);
		MessagesThread thread = loadResource(MessagesThread.class, threadId);

		List<SimpleOfflineMessage> sentMessages = new ArrayList<SimpleOfflineMessage>();

		for (UserData receiverData : receivers) {
			if (receiverData.getId() == senderId) {
				continue;
			}

			User receiver = loadResource(User.class, receiverData.getId());

			SimpleOfflineMessage message = new SimpleOfflineMessage();
			message.setSender(sender);
			message.setReceiver(receiver);
			message.setContent(text);
			message.setDateCreated(new Date());
			message.setRead(false);
			message.setMessageThread(thread);
			message = saveEntity(message);

			sentMessages.add(message);
		}

		thread.getMessages().addAll(sentMessages);
		thread.setLastUpdated(new Date());
		saveEntity(thread);

		return sentMessages;
	}*/
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Message createMessages(long senderId, List<UserData> receivers, String text,
			long threadId, String context) throws ResourceCouldNotBeLoadedException {
		User sender = loadResource(User.class, senderId);
		MessageThread thread = loadResource(MessageThread.class, threadId);

		Message message = new Message();

		Set<MessageParticipant> participants = new HashSet<>();
		MessageParticipant msgSender = new MessageParticipant();
		msgSender.setRead(false);
		msgSender.setSender(true);
		msgSender.setParticipant(sender);
		
		participants.add(msgSender);
		message.setSender(msgSender);
		
		for (UserData receiverData : receivers) {
			if (receiverData.getId() == senderId) {
				continue;
			}
			User receiver = loadResource(User.class, receiverData.getId());
			MessageParticipant msgReceiver = new MessageParticipant();
			msgReceiver.setRead(false);
			msgReceiver.setSender(false);
			msgReceiver.setParticipant(receiver);
			
			participants.add(msgReceiver);
		}
		
		
		message.setParticipants(participants);
		
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
		List<User> participants = userManager.loadUsers(participantIds);

		MessageThread messagesThread = new MessageThread();
		messagesThread.setCreator(creator);
		messagesThread.setParticipants(participants);

		Date now = new Date();
		messagesThread.setDateCreated(now);
		messagesThread.setLastUpdated(now);

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
				+ "WHERE participant1.id = :userid1 " + "AND participant2.id = :userid2";

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
	public List<MessageThread> getLatestUserMessagesThreads(User user, int page, int limit) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participants " + "WHERE :receiver IN (participants)  "
				+ "ORDER BY thread.lastUpdated DESC";

		Session session = this.persistence.openSession();
		List<MessageThread> result = null;

		try {
			result = session.createQuery(query).setEntity("receiver", user).setFirstResult(page * limit)
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

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Message> getUnreadSimpleOfflineMessages(User user, int page, int limit) {
		String query = "SELECT DISTINCT message " + "FROM Message message "
				+ "WHERE message.receiver = :receiver " + "AND message.read = :read "
				+ "ORDER BY message.dateCreated DESC";

		Session session = (Session) persistence.openSession();
		List<Message> result = null;

		try {
			result = session.createQuery(query).setEntity("receiver", user).setBoolean("read", false)
					.setFirstResult(page * limit).setMaxResults(limit).list();

			if (result != null) {
				return result;
			}
			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getSimpleOfflineMessages(User user, int page, int limit) {
		String query = "SELECT DISTINCT message " + "FROM Message as message "
				+ "LEFT JOIN message.receiver receiver " + "WHERE receiver = :receiver "
				+ "ORDER BY message.dateCreated DESC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query).setEntity("receiver", user)
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
				+ "WHERE thread.id = :threadId " + "ORDER BY message.dateCreated DESC";

		@SuppressWarnings("unchecked")
		List<Message> result = persistence.currentManager().createQuery(query)
				.setLong("threadId", threadId).setFirstResult(offset).setMaxResults(limit + 1).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Message> getMessagesForThread(MessageThread thread, int page, int limit,
			Session session) {
		String query = "SELECT DISTINCT message " + "FROM MessageThread thread " + "LEFT JOIN thread.messages message "
				+ "WHERE thread = :thread " + "ORDER BY message.dateCreated ASC";

		@SuppressWarnings("unchecked")
		List<Message> result = session.createQuery(query).setEntity("thread", thread)
				.setFirstResult(page * limit).setMaxResults(limit).list();

		if (result != null) {
			return result;
		}

		return new ArrayList<Message>();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MessageThread> getUserMessagesThreads(User user, int page, int limit) {
		String query = "SELECT DISTINCT thread " + "FROM MessageThread thread "
				+ "LEFT JOIN thread.participants participant " + "WHERE participant = :user "
				+ "ORDER BY thread.lastUpdated DESC";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query).setEntity("user", user)
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
				+ "LEFT JOIN thread.participants participants " + "WHERE :user IN (participants) "
				+ "ORDER BY thread.lastUpdated DESC ";

		@SuppressWarnings("unchecked")
		List<MessageThread> result = persistence.currentManager().createQuery(query).setEntity("user", user)
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
			boolean hasUnreadedMessages = containsUnreadedMessages(mThread, user);

			if (hasUnreadedMessages) {
				System.out.println("Has unreaded messages:" + user.getId() + " " + user.getLastname() + " "
						+ mtData.getParticipantsList());
			}

			mtData.setUnreaded(hasUnreadedMessages);
			mtData.setReaded(!hasUnreadedMessages);
			mtData.setSubject(mThread.getSubject());
			mtData.setId(mThread.getId());

			List<Message> messages = mThread.getMessages();

			List<MessageData> messagesData = new ArrayList<MessageData>();

			for (Message m : messages) {
				messagesData.add(new MessageData(m, user));
			}
			
			//why
			//mtData.setReaded(true);

			// for (MessageData mData : messagesData) {
			// if (mData.getActor().getId() != user.getId()) {
			// if (!mData.isReaded()) {
			// mtData.setReaded(false);
			// }
			// if (mtData.getLatestReceived() == null) {
			// mtData.setLatestReceived(mData);
			// } else if ((mData.getCreated().getTime() - 1) >
			// mtData.getLatestReceived().getCreated().getTime()) {
			// mtData.setLatestReceived(mData);
			// mtData.setReaded(false);
			// }
			// }
			// }
			mtData.setMessages(messagesData);

			List<UserData> participants = new ArrayList<UserData>();
			List<UserData> participantsWithoutLoggedUser = new ArrayList<UserData>();

			for (User u : mThread.getParticipants()) {
				// UserData userData = new UserData(u);
				UserData userData = UserDataFactory.createUserData(u);

				participants.add(userData);

				if (u.getId() != user.getId()) {
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

			mtData.setParticipantsListWithoutLoggedUser(buffer.toString());
			mtData.setParticipantsWithoutLoggedUser(participantsWithoutLoggedUser);
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
		return mtData;
	}

	private String createParticipantsList(MessageThread thread) {
		String participantsNames = null;

		if (thread != null) {
			List<User> participants = thread.getParticipants();
			participantsNames = thread.getCreator().getName() + " " + thread.getCreator().getLastname();
			Iterator<User> pIterator = participants.iterator();

			while (pIterator.hasNext()) {
				User participant = pIterator.next();

				if (!participant.equals(thread.getCreator())) {
					participantsNames = participantsNames + ", ";
					participantsNames = participantsNames + participant.getName() + " " + participant.getLastname();
				}
			}
		}
		return participantsNames;
	}

	private boolean containsUnreadedMessages(MessageThread thread, User user) {
		List<Message> messages = thread.getMessages();

		for (Message message : messages) {
			for(MessageParticipant mp : message.getParticipants()) {
				if (mp.getParticipant().equals(user)) {
					if (mp.isRead() == false) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	@Transactional(readOnly = false)
	public Message markAsRead(Message message, User user, Session session) {
		if (message != null) {
			for(MessageParticipant mp : message.getParticipants()) {
				if(mp.getParticipant().equals(user)) {
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

	/*@Override
	@Transactional(readOnly = false)
	public boolean markAsRead(long[] messageIds, User user, Session session) throws ResourceCouldNotBeLoadedException {
		boolean successful = true;

		for (int i = 0; i < messageIds.length; i++) {
			long msgId = messageIds[i];

			if (msgId > 0) {
				SimpleOfflineMessage message = (SimpleOfflineMessage) session.get(SimpleOfflineMessage.class, msgId);
				SimpleOfflineMessage updatedMessage = markAsRead(message, user, session);
				successful = successful && updatedMessage != null;
			}
		}
		return successful;
	}*/

	@Override
	public boolean markThreadAsRead(long threadId, User user) {
		Session session = this.getPersistence().openSession();
		MessageThread mThread = null;

		try {
			mThread = (MessageThread) session.get(MessageThread.class, threadId);

			if (mThread != null) {
				List<Message> messages = mThread.getMessages();

				for (Message offMessage : messages) {
					for(MessageParticipant mp : offMessage.getParticipants()) {
						if(mp.getParticipant().equals(user)) {
							if (!mp.isRead()) {
								mp.setRead(true);
							}
						}
					}
					session.save(offMessage);
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
	public Message sendMessage(long senderId, long receiverId, String msg) throws DbConnectionException{
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

			Message message = sendSimpleOfflineMessage(sender, receiverId,
					msg, messagesThread, null);

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

}
