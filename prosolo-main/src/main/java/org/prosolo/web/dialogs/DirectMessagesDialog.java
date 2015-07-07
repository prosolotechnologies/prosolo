package org.prosolo.web.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserData;
import org.prosolo.web.communications.data.MessageData;
import org.prosolo.web.home.MessagesBean;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
	
	private UserData receiver;
	public String messageContent;
	
	private List<MessageData> messages;
	
	@SuppressWarnings("unused")
	private long threadId;
	private String context;
	
	public void addReceiverData(UserData user, String context) {
		if (user.getId() != loggedUser.getUser().getId()) {
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
		SimpleOfflineMessage message = null;
		MessagesThread messagesThread = null;
		
		try {
			messagesThread = messagingManager.findMessagesThreadForUsers(loggedUser.getUser().getId(), receiver.getId());
			
			if (messagesThread == null) {
				List<Long> participantsIds = new ArrayList<Long>();
				participantsIds.add(receiver.getId());
				participantsIds.add(loggedUser.getUser().getId());
				
				messagesThread = messagingManager.createNewMessagesThread(
					loggedUser.getUser(), 
					participantsIds, 
					this.messageContent);
			}
			
			message = messagingManager.sendSimpleOfflineMessage(
					loggedUser.getUser(), 
					receiver.getId(), 
					this.messageContent, 
					messagesThread,
					context);

			messagesThread = messagingManager.merge(messagesThread);
			messagesThread.addMessage(message);
			messagingManager.saveEntity(messagesThread);
			message.setMessageThread(messagesThread);
			messagingManager.saveEntity(message);
			
			logger.debug("User "+loggedUser.getUser()+" sent a message to "+receiver+" with content: '"+this.messageContent+"'");
			
			List<UserData> participants = new ArrayList<UserData>();
			participants.add(new UserData(loggedUser.getUser()));
			
			PageUtil.fireSuccessfulInfoMessage("dmcomp:newDirectMessageFormGrowl", 
					"You have sent a message to " + MessagesBean.getReceiversListed(participants));
		} catch (ResourceCouldNotBeLoadedException e) {
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
