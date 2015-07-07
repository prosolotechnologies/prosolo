package org.prosolo.web.notification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.communications.data.MessageData;
import org.prosolo.web.communications.data.MessagesThreadData;
import org.prosolo.web.logging.LoggingNavigationBean;
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
	
	//@Autowired private MessagesThreadBean messagesThreadBean;

	private List<MessageData> messages;
	private List<MessagesThreadData> messagesThreads;
	private int unreadThreadsNo;
	@SuppressWarnings("unused")
	private Date lastViewed;
	private int messagesLimit = Settings.getInstance().config.application.notifications.topNotificationsToShow;
	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;
	private List<Long> unreadedThreads = new ArrayList<Long>();

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
			
			List<MessagesThread> mThreads = messagingManager.getLatestUserMessagesThreads(
					loggedUser.getUser(), 
					0,
					messagesLimit);
			
			if (mThreads != null) {
				this.messagesThreads = messagingManager.convertMessagesThreadsToMessagesThreadData(mThreads, loggedUser.getUser());
				
				for (MessagesThreadData mtData : this.messagesThreads) {
					if (!mtData.isReaded()) {
						this.unreadedThreads.add(mtData.getId());
						this.unreadThreadsNo++;
						System.out.println("Logged user:"+loggedUser.getLastName()+" unreaded:"+this.unreadThreadsNo+" from:"+mtData.getParticipantsList());
					}
				}
			}
		}
	}
	
	public void readMessages() {
		lastViewed = new Date();
		
		for (long msgThreadId : unreadedThreads) {
			this.setMessageThreadAsReaded(msgThreadId);
			
			for (MessagesThreadData mtData : this.messagesThreads) {
				if (mtData.getId() == msgThreadId) {
					if (!mtData.isReaded()) {
						mtData.setReaded(true);
						this.unreadThreadsNo--;
					}
				}
			}
		}
		unreadedThreads = new ArrayList<Long>();
	}
	
	public void addNewUnreadMessage(SimpleOfflineMessage message) {
		if (this.messagesThreads == null) {
			this.initMessages();
		}
		
		MessagesThread msgThread = message.getMessageThread();
		Session session = (Session) messagingManager.getPersistence().openSession();
		
		try {
			msgThread = (MessagesThread) session.merge(msgThread);
			
			long messageThreadId = msgThread.getId();
			
			if (!msgThread.getMessages().contains(message)) {
				msgThread.addMessage(message);
			}
			
			for (MessagesThreadData mThreadData : this.messagesThreads) {
				if (mThreadData.getId() == messageThreadId) {
					MessageData messageData = new MessageData(message);
					mThreadData.addMessage(messageData);
					
					if (!messageData.isReaded()) {
						if (!this.unreadedThreads.contains(mThreadData.getId())) {
							this.unreadedThreads.add(mThreadData.getId());
						}
						mThreadData.setReaded(false);
					}
					return;
				}
			}
			
			MessagesThreadData mThreadData = messagingManager.convertMessagesThreadToMessagesThreadData(msgThread, loggedUser.getUser());
			
			this.messagesThreads.add(mThreadData);
			
			if (!this.unreadedThreads.contains(mThreadData.getId())) {
				this.unreadedThreads.add(mThreadData.getId());
				
				if (!mThreadData.isReaded()) {
					this.unreadThreadsNo++;
				}
			}
			session.flush();
		}
		finally {
			HibernateUtil.close(session);
		}
	}
	
	public void addNewMessageThread(MessagesThread thread) {
		messagesThreads.add(new MessagesThreadData(thread, loggedUser.getUser()));
		
		if (messagesThreads.size() > messagesLimit) {
			messagesThreads = messagesThreads.subList(0, messagesLimit);
		}
	}

	public void updateMessageThread(MessagesThread thread) {
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
		
		messagesThreads.add(updatedMessageThread);
	}

	/*
	 * GETTERS / SETTERS
	 */
	public int getUnreadThreadsNo() {
		return this.unreadThreadsNo;
	}
	
	public void setUnreadThreadsNo(int unreadThreadsNo) {
		this.unreadThreadsNo = unreadThreadsNo;
	}
	
	public void setMessageThreadAsReaded(long msgThreadId){
		messagingManager.markThreadAsRead(msgThreadId,loggedUser.getUser().getId());
	}
	
	public List<MessageData> getMessages() {
		return messages;
	}
	
	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}
	
	public int getRefreshRate() {
		return refreshRate;
	}
	
	public int getUnreadMessagesNo() {
		if (this.unreadedThreads != null) {
			return this.unreadedThreads.size();
		} else
			return 0;
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

	public void setMessagesThreads(List<MessagesThreadData> messagesThreads) {
		this.messagesThreads = messagesThreads;
	}


}
