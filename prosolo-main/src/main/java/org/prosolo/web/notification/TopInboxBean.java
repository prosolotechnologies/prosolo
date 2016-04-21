package org.prosolo.web.notification;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.messaging.data.MessagesThreadData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "topInboxBean")
@Component("topInboxBean")
@Scope("session")
public class TopInboxBean implements Serializable {
	
	private static final long serialVersionUID = -6523581537208723654L;

	private static Logger logger = Logger.getLogger(TopInboxBean.class);
	
	@Autowired private MessagingManager messagingManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private LoggingNavigationBean loggingNavigationBean;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private List<MessageData> messages;
	private List<MessagesThreadData> messagesThreads;
	private int unreadThreadsNo;
	private int messagesLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;

	@PostConstruct
	public void initUnreadMessageNo() { 
		User user = loggedUser.getUser();
		
		if (user != null){
			initMessages();
		}
	}
	
	public void initMessages() {
		if (messagesThreads == null) {
			logger.debug("Initializing messages");
			
			List<MessageThread> mThreads = messagingManager.getLatestUserMessagesThreads(
					loggedUser.getUser(), 
					0,
					messagesLimit);
			
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
	
	public void readMessages() {
		List<Long> unreadThreadIds = new LinkedList<Long>();
		
		for (MessagesThreadData mtData : this.messagesThreads) {
			if (!mtData.isReaded()) {
				mtData.setReaded(true);
				unreadThreadIds.add(mtData.getId());
			}
		}
		
		this.unreadThreadsNo = 0;
		
		final List<Long> unreadThreadIds1 = unreadThreadIds;

		taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
            	for (Long threadId : unreadThreadIds1) {
            		messagingManager.markThreadAsRead(threadId, loggedUser.getUser().getId());
            	}
            }
		});
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

	/*
	 * GETTERS / SETTERS
	 */
	public int getUnreadThreadsNo() {
		return this.unreadThreadsNo;
	}
	
	public List<MessageData> getMessages() {
		return messages;
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

}
