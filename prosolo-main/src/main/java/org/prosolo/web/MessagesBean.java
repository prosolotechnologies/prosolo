package org.prosolo.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.communications.data.MessageData;
import org.prosolo.web.communications.data.MessagesThreadData;
import org.prosolo.web.useractions.data.NewPostData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	
	@Autowired private MessagingManager messagingManager;
	@Autowired private LoggedUserBean loggedUser;
	@Inject private UrlIdEncoder idEncoder;
	
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
	
	public void init() {
		
		decodedThreadId = idEncoder.decodeId(threadId);
		
		boolean hasAccess = tryToInitMessages();

		if (!hasAccess) {
			try {
				PageUtil.sendToAccessDeniedPage();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	private boolean tryToInitMessages() {
		MessagesThread thread = null;
		
		if (decodedThreadId == 0) {
			thread = messagingManager.getLatestMessageThread(loggedUser.getUser());
			
			if (thread != null) {
				return initializeThreadData(thread);
			}
			return false;
		}
		
		if (loggedUser != null && loggedUser.isLoggedIn()) {
			if (decodedThreadId > 0) {
				try {
					thread = messagingManager.get(MessagesThread.class, decodedThreadId);
					
					if (thread == null) {
						logger.info("User "+loggedUser.getUser()+" tried to open messages page for nonexisting messages thread with id: " + threadId);
						
						return false;
					}
				
					return initializeThreadData(thread);
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error(e);
					
					return false;
				}
			}
		} else {
			logger.info("Not logged-in user tried to open messages page with thread id: " + threadId);
			
			return false;
		}
		return true;
	}

	private boolean initializeThreadData(MessagesThread thread) {
		this.threadData = new MessagesThreadData(thread, loggedUser.getUser());
		this.receivers = threadData.getParticipants();
		
		if (!threadData.containsParticipant(loggedUser.getUser().getId())) {
			logger.info("User "+loggedUser.getUser()+" doesn't have permisisons to view messages thread with id " + threadId);
			
			return false;
		} else {
			this.messages = new LinkedList<MessageData>();
			loadMessages();
		}
		return true;
	}
	
	public void changeThread(MessagesThreadData threadData) {
		MessagesThread thread;
		try {
			thread = messagingManager.get(MessagesThread.class, threadData.getId());
			initializeThreadData(thread);
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
			
			PageUtil.fireErrorMessage("There was an error with loading this cnversation");
		}
	}

	private void loadMessages() {
		List<SimpleOfflineMessage> mess = messagingManager.getMessagesForThread(this.threadData.getId(), messages.size(), limit);
		
		if (mess.size() > limit) {
			this.loadMore = true;

			mess = mess.subList(0, this.limit);
		} else {
			this.loadMore = false;
		}
		
		for (SimpleOfflineMessage message : mess) {
			this.messages.add(0, new MessageData(message, loggedUser.getUser()));
		}
		
		Collections.sort(this.messages);
	}
	
	public void createNewPost(){
		messageData.setText(StringUtil.cleanHtml(messageData.getText()));
		
		try {
			SimpleOfflineMessage message = messagingManager.sendMessages(
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
	
	public static String getReceiversListed(List<UserData> users){
		String userNames = "";
		int ind = 0;
		
		for (UserData us : users) {
			if (ind > 0)
				userNames = userNames + ", ";
			ind++;
			userNames = userNames + us.getName();
		}
		return userNames;
	}
	
	public void addMessage(SimpleOfflineMessage message) {
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

}
