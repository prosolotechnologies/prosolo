package org.prosolo.web.dialogs;

import org.apache.log4j.Logger;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zoran Jeremic
 * @date Jul 12, 2012
 */
@ManagedBean(name = "directMessagesDialog")
@Component("directMessagesDialog")
@Scope("view")
public class DirectMessagesDialog implements Serializable {

	private static final long serialVersionUID = 2771725028311112550L;
	
	private static Logger logger = Logger.getLogger(DirectMessagesDialog.class);
	
	@Autowired private LoggingNavigationBean actionLogger;
	@Autowired private MessagingManager messagingManager;
	@Autowired private LoggedUserBean loggedUser;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	private UserData receiver;
	public String messageContent;
	
	private List<MessageData> messages;
	
	@SuppressWarnings("unused")
	private long threadId;
	private String context;
	
	public void addReceiverData(UserData user, String context) {
		if (user.getId() != loggedUser.getUserId()) {
			receiver = user;
		}
		
		this.context = context;
		
		setMessageContent(null);
		
		actionLogger.logServiceUse(
				ComponentName.DIRECT_MESSAGE_DIALOG, 
				"context", context,
				"user", String.valueOf(user.getId()));
	}
	
	public void sendMessage() {
		try {
			messagingManager.sendMessageDialog(loggedUser.getUserId(), receiver.getId(), this.messageContent,
					loggedUser.getUserContext());
			logger.debug("User "+loggedUser.getUserId()+" sent a message to "+receiver+" with content: '"+this.messageContent+"'");
			
			List<UserData> participants = new ArrayList<UserData>();
			participants.add(new UserData(loggedUser.getUserId(), loggedUser.getFullName(), loggedUser.getAvatar()));

			PageUtil.fireSuccessfulInfoMessage("dmcomp:newDirectMessageFormGrowl", "Your message is sent");
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	public List<MessageData> getMessages() {
		return messages;
	}
	
	public UserData getReceiver() {
		return receiver;
	}

	public String getContext() {
		return context;
	}
	
	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
}
