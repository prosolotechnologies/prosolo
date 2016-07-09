package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.messaging.data.MessageData;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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
	@Autowired private EventFactory eventFactory;
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
			Message message = messagingManager.sendMessage(loggedUser.getUserId(), receiver.getId(), this.messageContent);
			logger.debug("User "+loggedUser.getUserId()+" sent a message to "+receiver+" with content: '"+this.messageContent+"'");
			
			List<UserData> participants = new ArrayList<UserData>();
			participants.add(new UserData(loggedUser.getUserId(), loggedUser.getFullName(), loggedUser.getAvatar()));
			
			final Message message1 = message;
			
			taskExecutor.execute(new Runnable() {
	            @Override
	            public void run() {
	            	try {
	            		Map<String, String> parameters = new HashMap<String, String>();
	            		parameters.put("context", context);
	            		parameters.put("user", String.valueOf(receiver.getId()));
	            		parameters.put("message", String.valueOf(message1.getId()));
	            		eventFactory.generateEvent(EventType.SEND_MESSAGE, loggedUser.getUserId(), message1, null, parameters);
	            	} catch (EventException e) {
	            		logger.error(e);
	            	}
	            }
			});
			
			PageUtil.fireSuccessfulInfoMessage("dmcomp:newDirectMessageFormGrowl", "Message sent");
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
