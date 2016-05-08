package org.prosolo.web.messaging;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;
import org.prosolo.web.notification.TopInboxBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "messagesBean")
@Component("messagesBean")
@Scope("session")
public class MessagesBean implements Serializable {
	
	private static final long serialVersionUID = -7914658400194958136L;

	private static Logger logger = Logger.getLogger(MessagesBean.class);
	
	@Autowired private MessagingManager messagingManager;
	@Autowired private LoggedUserBean loggedUser;
	@Inject private UrlIdEncoder idEncoder;
	@Autowired private TopInboxBean topInboxBean;
	
	protected List<UserData> receivers;
	
	private NewPostData messageData = new NewPostData();
	
	private MessagesThreadData threadData;
	private List<MessageData> messages;
	private String threadId;
	private String context;
	private int limit = 5;
	private boolean loadMore;
	private boolean noMessageThreads;
	private long decodedThreadId;
	private boolean archiveView;
	
	//TopInboxBean fields
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	private List<MessagesThreadData> messagesThreads;
	private int unreadThreadsNo;
	private int messagesLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;
	
	private enum MessageProcessingResult {
		OK, FORBIDDEN, ERROR, NO_MESSAGES
	}
	
	public void init() {
		
		decodedThreadId = idEncoder.decodeId(threadId);
		initMessageThreadData();
		MessageProcessingResult result = tryToInitMessages();

		if (result.equals(MessageProcessingResult.FORBIDDEN)) {
			try {
				PageUtil.sendToAccessDeniedPage();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	private void initMessageThreadData() {
			if (messagesThreads == null) {
				logger.debug("Initializing messages");
				
				List<MessageThread> mThreads = messagingManager.getLatestUserMessagesThreads(
						loggedUser.getUser(), 
						0,messagesLimit,archiveView);
				
				if (mThreads != null) {
					this.messagesThreads = messagingManager.convertMessagesThreadsToMessagesThreadData(mThreads, loggedUser.getUser());
					
					for (MessagesThreadData mtData : this.messagesThreads) {
						if (!mtData.isReaded()) {
							this.unreadThreadsNo++;
						}
					}
				}
			}
	}

	private MessageProcessingResult tryToInitMessages() {
		MessageThread thread = null;
		
		if (decodedThreadId == 0) {
			thread = messagingManager.getLatestMessageThread(loggedUser.getUser());
			
			if (thread != null) {
				return initializeThreadData(thread);
			}
			else return MessageProcessingResult.NO_MESSAGES;
		}
		
		if (loggedUser != null && loggedUser.isLoggedIn()) {
			if (decodedThreadId > 0) {
				try {
					thread = messagingManager.get(MessageThread.class, decodedThreadId);
					
					if (thread == null) {
						logger.info("User "+loggedUser.getUser()+" tried to open messages page for nonexisting messages thread with id: " + threadId);
						return MessageProcessingResult.FORBIDDEN;
					}
				
					return initializeThreadData(thread);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
					return MessageProcessingResult.ERROR;
				}
			}
		} 
		else {
			logger.info("Not logged-in user tried to open messages page with thread id: " + threadId);
			return MessageProcessingResult.FORBIDDEN;
		}
		return MessageProcessingResult.OK;
	}

	private MessageProcessingResult initializeThreadData(MessageThread thread) {
		this.threadData = new MessagesThreadData(thread, loggedUser.getUser());
		this.receivers = threadData.getParticipants();
		
		if (!threadData.containsParticipant(loggedUser.getUser().getId())) {
			logger.info("User "+loggedUser.getUser()+" doesn't have permisisons to view messages thread with id " + threadId);
			return MessageProcessingResult.FORBIDDEN;
		} else {
			this.messages = new LinkedList<MessageData>();
			loadMessages();
		}
		return MessageProcessingResult.OK;
	}
	
	public void changeThread(MessagesThreadData threadData) {
		MessageThread thread;
		try {
			thread = messagingManager.get(MessageThread.class, threadData.getId());
			initializeThreadData(thread);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			
			PageUtil.fireErrorMessage("There was an error with loading this cnversation");
		}
	}
	
	public void addNewMessageThread(MessageThread thread) {
		messagesThreads.add(0, new MessagesThreadData(thread, loggedUser.getUser()));
		
		if (messagesThreads.size() > messagesLimit) {
			messagesThreads = messagesThreads.subList(0, messagesLimit);
		}
	}
	
	public void updateMessageThread(MessageThread thread) {
		MessagesThreadData updatedMessageThread = new MessagesThreadData(thread, loggedUser.getUser());
		
		// find the current instance of this thread and remove it
		Iterator<MessagesThreadData> iterator = messagesThreads.iterator();
		
		while (iterator.hasNext()) {
			MessagesThreadData threadData = (MessagesThreadData) iterator.next();
			
			if (threadData.getId() == thread.getId()) {
				iterator.remove();
				break;
			}
		}
		
		messagesThreads.add(0, updatedMessageThread);
		
		// if logged in user didn't create a message, update the counter of unread messages
		if (thread.getMessages().get(0).getSender().getId() == loggedUser.getUser().getId()) {
			unreadThreadsNo++;
		}
	}

	private void loadMessages() {
		ThreadParticipant userParticipent = messagingManager.findParticipation(threadData.getId(),loggedUser.getUser().getId());
		List<Message> unreadMessages = messagingManager.getUnreadMessages(threadData.getId(), userParticipent.getLastReadMessage());
		List<Message> readMessages = new ArrayList<>();
		//if number of unread messages >= limit, pull 2 already read ones and join them with new ones
		if (unreadMessages.size() >= limit) {
			readMessages = messagingManager.getMessagesBeforeMessage(threadData.getId(),userParticipent.getLastReadMessage(),2);
		}
		else {
			//shift standard pagination for the number of unread messages (first result must be "higher" for that number, last result must be "lower")
			int startOffset = messages.size();
			int endOffset = limit - unreadMessages.size();
			readMessages = messagingManager.getMessagesForThread(threadData.getId(),startOffset, endOffset);
		}
		processMessageData(unreadMessages,readMessages);
		if(!archiveView) {
			markThreadRead();
		}
		
	}

	private void markThreadRead() {
		//save read info to database
		messagingManager.markThreadAsRead(threadData.getId(), loggedUser.getUser().getId());
		//mark current thread read
		for(MessagesThreadData messageThreadData : messagesThreads) {
			if(messageThreadData.getId() == threadData.getId()) {
				messageThreadData.setReaded(true);
			}
		}
		threadData.setReaded(true);
	}

	private void processMessageData(List<Message> unreadMessages, List<Message> readMessages) {
		
		if((readMessages.size() + unreadMessages.size()) > limit) {
			loadMore = true;
			//as getMessagesForThread returns one result more from what you ask, remove oldest read message (order returned is ASC)
			//but only if we are not in "all unread messages + 2 read messages" mode
			if(unreadMessages.size() < limit && readMessages.size() > 0) {
				readMessages.remove(0);
			}
		}
		for (Message message : unreadMessages) {
			this.messages.add(new MessageData(message, loggedUser.getUser(),false));
		}
		for (Message message : readMessages) {
			this.messages.add(new MessageData(message, loggedUser.getUser(),true));
		}
	}
	
	public void createNewPost(){
		messageData.setText(StringUtil.cleanHtml(messageData.getText()));
		
		try {
			Message message = messagingManager.sendMessages(
					loggedUser.getUser().getId(), 
					receivers,
					this.messageData.getText(), 
					threadData.getId(), 
					context);
			
			//SimpleOfflineMessage message = messages.get(0);
			this.messages.add(new MessageData(message, loggedUser.getUser()));

			PageUtil.fireSuccessfulInfoMessage("Message sent");
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("There was an error sending the message");
		}
				
		reset();
	}

	public void reset() {
		messageData = new NewPostData();
	}
	
	public void loadMore() {
		loadMessages();
	}
	
	
	public void addMessage(Message message) {
		messages.add(new MessageData(message, loggedUser.getUser()));
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	
	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public NewPostData getMessageData() {
		return messageData;
	}
	
	public List<MessageData> getMessages() {
		return messages;
	}

	public String getContext() {
		return context;
	}

	public boolean isLoadMore() {
		return loadMore;
	}

	public boolean isNoMessageThreads() {
		return noMessageThreads;
	}

	public MessagesThreadData getThreadData() {
		return threadData;
	}
	
	public List<MessageData> getReadMessages() {
		return getMessagesCoditionaly((msg) -> msg.isReaded());
	}
	
	public List<MessageData> getUnreadMessages() {
		return getMessagesCoditionaly((msg) -> !msg.isReaded());
	}
	
	public List<MessageData> getMessagesCoditionaly(Predicate<MessageData> predicate) {
		return messages.stream().filter(predicate).collect(Collectors.toList());
	}
	
	/*
	 * GETTERS / SETTERS from TopInboxBean
	 */
	public int getUnreadThreadsNo() {
		return this.unreadThreadsNo;
	}
	
	public int getRefreshRate() {
		return refreshRate;
	}
	
	public void logInboxServiceUse(){
		loggingNavigationBean.logServiceUse(
				ComponentName.INBOX,
				"action",  "openInbox",
				"numberOfUnreadThreads", String.valueOf(this.unreadThreadsNo));
	}

	public List<MessagesThreadData> getMessagesThreads() {
		return messagesThreads;
	}

	public boolean isArchiveView() {
		return archiveView;
	}

	public void setArchiveView(boolean archiveView) {
		this.archiveView = archiveView;
		messagesThreads = null;
		init();
	}
	
	public void increaseLimit() {
		this.limit += 5;
		init();
	}
	
}
