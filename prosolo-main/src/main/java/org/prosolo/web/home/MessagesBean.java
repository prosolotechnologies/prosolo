package org.prosolo.web.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MessagesBean implements Serializable {

	private static final long serialVersionUID = 6303696800061856888L;
	
	private static Logger logger = Logger.getLogger(MessagesBean.class);
	
	@Autowired protected LoggedUserBean loggedUserBean;

	protected List<UserData> receivers;
	public String messageContent;
	public String messageSubject;
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}

	public String getMessageSubject() {
		return messageSubject;
	}

	public void setMessageSubject(String messageSubject) {
		this.messageSubject = messageSubject;
	}

	public MessagesBean(){
		receivers = new ArrayList<UserData>();
	}
	
	public static String getReceiversListed(Collection<UserData> users){
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
	
	protected void sendFeedbackMessage(String textToShow){
		PageUtil.fireSuccessfulInfoMessage(textToShow, this.messageContent);
	}
	
	protected void sendFeedbackMessageWithoutContent(String textToShow){
		FacesContext.getCurrentInstance().addMessage(
				FacesMessage.FACES_MESSAGES,
				new FacesMessage(textToShow));
	}
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	
	public List<UserData> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<UserData> receivers) {
		this.receivers = receivers;
	}
	
}
