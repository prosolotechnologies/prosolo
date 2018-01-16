package org.prosolo.web.messaging;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.messaging.ThreadParticipant;
import org.prosolo.common.event.context.data.PageContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;
import org.prosolo.web.notification.TopInboxBean;
import org.prosolo.web.search.SearchPeopleBean;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author "Nikola Milikic"
 *
 */
@ManagedBean(name = "messagesBean")
@Component("messagesBean")
@Scope("view")
public class MessagesBean implements Serializable {
	
	private static final long serialVersionUID = -7914658400194958136L;

	private static Logger logger = Logger.getLogger(MessagesBean.class);
	
	@Inject private MessagingManager messagingManager;
	@Inject private LoggedUserBean loggedUser;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ThreadPoolTaskExecutor taskExecutor;
	@Inject private TopInboxBean topInboxBean;
	@Autowired private SearchPeopleBean searchPeopleBean;
	
	protected List<UserData> receivers;
	
	private NewPostData messageData = new NewPostData();
	
	private MessagesThreadData threadData;
	private List<MessageData> messages;
	private String threadId;
	private int limit = 5;
	private boolean loadMore;
	private long decodedThreadId;
	//variables used for controlling component displays
	private boolean archiveView;
	private boolean newMessageView;
	
	private String messageText = "";
	
	private List<MessagesThreadData> messagesThreads;
	private int messagesLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private List<Long> newMessageThreadParticipantIds = new ArrayList<>();
	
	private enum MessageProcessingResult {
		OK, FORBIDDEN, ERROR, NO_MESSAGES
	}
	
	public void init() {
		messageText = "";
		newMessageView = false;
		newMessageThreadParticipantIds.clear();
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
			if (CollectionUtils.isEmpty(messagesThreads)) {
				logger.debug("Initializing messages");
				
				List<MessageThread> mThreads = messagingManager.getLatestUserMessagesThreads(
						loggedUser.getUserId(), 
						0,messagesLimit,archiveView);
				
				if (mThreads != null) {
					this.messagesThreads = messagingManager.convertMessagesThreadsToMessagesThreadData(mThreads, loggedUser.getUserId());
				}
			}
	}

	/**
	 * DEPENDS ON initMessageThreadData, must be called after that
	 */
	private MessageProcessingResult tryToInitMessages() {
		MessageThread thread = null;
		String page = PageUtil.getPostParameter("page");
		String context = PageUtil.getPostParameter("context");
		page = (page != null) ? page : "messages";
		context = (context != null) ? context : "name:messages";
		UserContextData userContext = loggedUser.getUserContext(new PageContextData(page, context, null));

		if (decodedThreadId == 0) {
			try {
				thread = messagingManager.getLatestMessageThread(loggedUser.getUserId(), archiveView, userContext);
			} catch (Exception e) {
				logger.error(e);
			}

			if (thread != null) {
				return initializeThreadData(thread);
			}
			else return MessageProcessingResult.NO_MESSAGES;
		}
		
		if (loggedUser != null && loggedUser.isLoggedIn()) {
			if (decodedThreadId > 0) {
				try {
					thread = messagingManager.getAndMarkMessageThreadAsRead(decodedThreadId,userContext);

					if (thread == null || !userShouldSeeThread(thread)) {
						logger.warn("User "+loggedUser.getUserId()+" tried to access thread with id: " + threadId +" that either does not exist, is deleted for him, or is not hisown");
						return MessageProcessingResult.FORBIDDEN;
					}
					else {
						return initializeThreadData(thread);
					}
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
		this.threadData = new MessagesThreadData(thread, loggedUser.getUserId());
		this.receivers = threadData.getParticipants();
		
		if (!threadData.containsParticipant(loggedUser.getUserId())) {
			logger.info("User "+loggedUser.getUserId()+" doesn't have permisisons to view messages thread with id " + threadId);
			return MessageProcessingResult.FORBIDDEN;
		} else {
			this.messages = new LinkedList<MessageData>();
			loadMessages();
		}
		return MessageProcessingResult.OK;
	}
	
	public void changeThread(MessagesThreadData threadData) {
		MessageThread thread = null;
		try {
			String page = PageUtil.getPostParameter("page");
			String context = PageUtil.getPostParameter("context");
			page = (page != null) ? page : "messages";
			context = (context != null) ? context : "name:messages";
			UserContextData userContext = loggedUser.getUserContext(new PageContextData(page, context, null));
			//if we were on "newView", set it to false so we do not see user dropdown (no need for full init())
			newMessageView = false;
			thread = messagingManager.getAndMarkMessageThreadAsRead(decodedThreadId, userContext);
			initializeThreadData(thread);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			
			PageUtil.fireErrorMessage("There was an error with loading this cnversation");
		}
	}

	private void loadMessages() {
		ThreadParticipant userParticipent = messagingManager.findParticipation(threadData.getId(),loggedUser.getUserId());
		if(!userParticipent.isDeleted()) {
			List<Message> unreadMessages = messagingManager.getUnreadMessages(threadData.getId(), userParticipent.getLastReadMessage(), userParticipent.getShowMessagesFrom());
			List<Message> readMessages = new ArrayList<>();
			//if number of unread messages >= limit, pull 2 already read ones and join them with new ones
			if (unreadMessages.size() >= limit) {
				readMessages = messagingManager.getMessagesBeforeMessage(threadData.getId(),userParticipent.getLastReadMessage(),2, userParticipent.getShowMessagesFrom());
			}
			else {
				//shift standard pagination for the number of unread messages (first result must be "higher" for that number, last result must be "lower")
				int startOffset = messages.size();
				int endOffset = limit - unreadMessages.size();
				readMessages = messagingManager.getMessagesForThread(threadData.getId(), startOffset, endOffset,userParticipent.getShowMessagesFrom());
			}
			processMessageData(unreadMessages,readMessages);
			if(!archiveView) {
				markThreadRead();
			}
		}
		else {
			logger.warn("User "+loggedUser.getUserId()+" tried to access thread with id: " + threadId +" that is deleted for him");
		}
		
	}

	private void markThreadRead() {
		//save read info to database
		messagingManager.markThreadAsRead(threadData.getId(), loggedUser.getUserId());
		//only mark t read in session if DB operation was a success
		topInboxBean.markThreadRead(threadData.getId());
		//mark current thread read
		for(MessagesThreadData messageThreadData : messagesThreads) {
			if(messageThreadData.getId() == threadData.getId()) {
				messageThreadData.setReaded(true);
			}
		}
		threadData.setReaded(true);
	}

	private void processMessageData(List<Message> unreadMessages, List<Message> readMessages) {
		
		//getMessagesForThread always returns one more message than what we asked, so it serves to set the flag
		if((readMessages.size() + unreadMessages.size()) > limit) {
			loadMore = true;
		}
		else {
			loadMore = false;
		}
		
		//as getMessagesForThread fetches one more than what we asked for (for setting the flag), but just one when there is one
		removeOverlappingmessages(unreadMessages, readMessages);
		
		for (Message message : unreadMessages) {
			this.messages.add(new MessageData(message, loggedUser.getUserId(), false));
		}
		for (Message message : readMessages) {
			this.messages.add(new MessageData(message, loggedUser.getUserId(), true));
		}
		//As we sorted them by date DESC, now show them ASC (so last message will be last one created)
		Collections.sort(messages,(a,b) -> a.getCreated().compareTo(b.getCreated()));
	}


	public void sendMessage(){
		try {
			//TODO what is the context?
			Message message = null;
			String page = PageUtil.getPostParameter("page");
			String context = PageUtil.getPostParameter("context");
			page = (page != null) ? page : "messages";
			context = (context != null) ? context : "name:messages";
			UserContextData userContext = loggedUser.getUserContext(new PageContextData(page, context, null));

			if(CollectionUtils.isNotEmpty(newMessageThreadParticipantIds)) {
				//new recipients have been set, send message to them (and create or re-use existing thread)
				message = messagingManager.sendMessageParticipantsSet(loggedUser.getUserId(), newMessageThreadParticipantIds.get(0),
						messageText, userContext); //single recipient, for now
				initializeThreadData(message.getMessageThread());
			}
			else {
				//no recipients set, assume current thread is used
				messagingManager.sendMessages(loggedUser.getUserId(),
						threadData.getParticipants(), messageText, threadData.getId(), "", userContext);
			}
			//at this point, threadData is initialized (either through init method, or now, by sending very first message)
			logger.debug("User "+loggedUser.getUserId()+" sent a message to thread " + threadData.getId()+ " with content: '"+this.messageText+"'");
			PageUtil.fireSuccessfulInfoMessage("messagesFormGrowl", "Your message is sent");
			//set archived to false, as sending message unarchives thread
			archiveView = false;
			//reset message data, so we can re-fetch messages and messages threads
			messagesThreads = null;
			init();
		} catch (Exception e) {
			logger.error("Exception while sending message", e);
		}
	}
	
	public void setupNewMessageThreadRecievers() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String ids = params.get("newThreadRecipients");
		if(StringUtils.isBlank(ids)){
			logger.error("User "+loggedUser.getUserId()+" tried to send message with empty recipient list");
			PageUtil.fireErrorMessage("messagesFormGrowl", "Unable to send message");
		}
		else {
			newMessageThreadParticipantIds = getRecieverIdsFromParameters(ids);

		}
	}

	private List<Long> getRecieverIdsFromParameters(String ids) {
		return Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
	}

	public void loadMore() {
		loadMessages();
	}
	
	
	public void addMessage(Message message) {
		messages.add(new MessageData(message, loggedUser.getUserId()));
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

	public boolean isLoadMore() {
		return loadMore;
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
	
	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	
	
	
//	public void logInboxServiceUse(){
//		loggingNavigationBean.logServiceUse(
//				ComponentName.INBOX,
//				"action",  "openInbox",
//				"numberOfUnreadThreads", String.valueOf(this.unreadThreadsNo));
//	}

	public List<Long> getNewMessageThreadParticipantIds() {
		return newMessageThreadParticipantIds;
	}

	public void setNewMessageThreadParticipantIds(List<Long> newMessageThreadParticipantIds) {
		this.newMessageThreadParticipantIds = newMessageThreadParticipantIds;
	}

	public TopInboxBean getTopInboxBean() {
		return topInboxBean;
	}

	public void setTopInboxBean(TopInboxBean topInboxBean) {
		this.topInboxBean = topInboxBean;
	}

	public List<MessagesThreadData> getMessagesThreads() {
		return messagesThreads;
	}

	public boolean isArchiveView() {
		return archiveView;
	}
	
	public boolean isNewMessageView() {
		return newMessageView;
	}

	public void setNewMessageView(boolean newMessageView) {
		this.newMessageView = newMessageView;
		
		if (newMessageView) {
			searchPeopleBean.resetSearch();
		}
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
	
	public void resetLimit() {
		this.limit = 5;
		init();
	}
	
	public void archiveCurrentThread() {
		messagesThreads = null;
		messagingManager.archiveThread(threadData.getId(), loggedUser.getUserId());
		init();
	}
	
	public void deleteCurrentThread() {
		messagesThreads = null;
		messagingManager.markThreadDeleted(threadData.getId(), loggedUser.getUserId());
		init();
	}
	
	private boolean userShouldSeeThread(MessageThread thread) {
		ThreadParticipant userParticipent = thread.getParticipant(loggedUser.getUserId());
		return userParticipent != null && !(userParticipent.isDeleted());
	}
	
	private void removeOverlappingmessages(List<Message> unreadMessages, List<Message> readMessages) {
		for(Message unreadMessage : unreadMessages) {
			Message overlappingMessage = findById(unreadMessage.getId(), readMessages);
			if(overlappingMessage != null) {
				removeById(unreadMessage.getId(), readMessages);
			}
		}
	}

	private void removeById(long id, List<Message> messages) {
		Iterator<Message> iterator = messages.iterator();
		while(iterator.hasNext()) {
			Message message = iterator.next();
			if(message.getId()==id) {
				iterator.remove();
				break;
			}
		}
		
	}

	private Message findById(long id, List<Message> messages) {
		for(Message message : messages) {
			if(message.getId()==id){
				return message;
			}
		}
		return null;
	}	
}
