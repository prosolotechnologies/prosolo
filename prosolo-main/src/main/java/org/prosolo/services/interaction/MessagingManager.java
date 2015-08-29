package org.prosolo.services.interaction;

import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.communications.data.MessagesThreadData;

public interface MessagingManager extends AbstractManager {
	
	List<SimpleOfflineMessage> sendMessages(long senderId, List<UserData> receivers, String text, long threadId, String context) throws ResourceCouldNotBeLoadedException;

	SimpleOfflineMessage sendSimpleOfflineMessage(long senderId, long receiverId,
			String content, long threadId, String context) throws ResourceCouldNotBeLoadedException;

	SimpleOfflineMessage sendSimpleOfflineMessage(User sender, long receiverId,
			String content, MessagesThread messagesThread, String context) throws ResourceCouldNotBeLoadedException;
	
	List<SimpleOfflineMessage> getUnreadSimpleOfflineMessages(User user,
			int page, int limit);

	List<SimpleOfflineMessage> getSimpleOfflineMessages(User user, int page, int limit);

	List<MessagesThread> getUserMessagesThreads(User user, int page, int limit);

	MessagesThread getMessagesThreadForPrevoiusMessage(
			SimpleOfflineMessage previousMessage);

	MessagesThread createNewMessagesThread(User creator, List<Long> participantIds, String subject);

	List<SimpleOfflineMessage> getMessagesForThread(long threadId, int page, int limit);

	List<MessagesThreadData> convertMessagesThreadsToMessagesThreadData(List<MessagesThread> mThreads, User user);

	MessagesThreadData convertMessagesThreadToMessagesThreadData(
			MessagesThread mThread, User user);

	boolean markAsRead(long[] messageIds, Session session) throws ResourceCouldNotBeLoadedException;

	SimpleOfflineMessage markAsRead(SimpleOfflineMessage message, Session session);

	MessagesThread findMessagesThreadForUsers(long user1Id, long user2Id);

	List<MessagesThread> getLatestUserMessagesThreads(User user, int page, int limit);

	boolean markThreadAsRead(long threadId, long receiverId);

	List<SimpleOfflineMessage> getMessagesForThread(MessagesThread thread,
			int page, int limit, Session session);

	MessagesThread getLatestMessageThread(User user);

}
