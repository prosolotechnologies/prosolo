package org.prosolo.web.manage.students;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.activitywall.data.UserDataFactory;
import org.prosolo.web.util.PageUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "sendMessageBean")
@Component
@Scope("view")
public class SendMessageBean implements Serializable {

	private static final long serialVersionUID = 6423542277691747720L;

	private static Logger logger = Logger.getLogger(SendMessageBean.class);
	
	@Inject 
	private LoggedUserBean loggedUserBean;
	@Inject 
	private MessagingManager messagingManager;
	@Inject 
	private EventFactory eventFactory;
	@Inject 
	@Qualifier("taskExecutor") 
	private ThreadPoolTaskExecutor taskExecutor;
	
	private String message;

	public void sendMessage(long receiverId, String receiverFullName) {
		if(receiverId != loggedUserBean.getUser().getId()) {
			try {
				Message message = messagingManager.sendMessage(loggedUserBean.getUser().getId(), 
						receiverId, this.message);
				logger.debug("User "+loggedUserBean.getUser()+" sent a message to " + receiverId +
						" with content: '"+message+"'");
				
				List<UserData> participants = new ArrayList<UserData>();
				participants.add(UserDataFactory.createUserData(loggedUserBean.getUser()));
				
				String page = PageUtil.getPostParameter("page");
				String lContext = PageUtil.getPostParameter("learningContext");
				String service = PageUtil.getPostParameter("service");
				
				final Message message1 = message;
				final User user = loggedUserBean.getUser();
				
				taskExecutor.execute(new Runnable() {
		            @Override
		            public void run() {
		            	try {
		            		Map<String, String> parameters = new HashMap<String, String>();
		            		//parameters.put("context", createContext());
		            		parameters.put("user", String.valueOf(receiverId));
		            		parameters.put("message", String.valueOf(message1.getId()));
		            		eventFactory.generateEvent(EventType.SEND_MESSAGE, user, message1, 
		            				null, page, lContext, service, parameters);
		            	} catch (EventException e) {
		            		logger.error(e);
		            	}
		            }
				});
				this.message = "";
				PageUtil.fireSuccessfulInfoMessage("profileGrowl", 
						"You have sent a message to " + receiverFullName);
			} catch (Exception e) {
				logger.error(e);
			}
		}
		else {
			PageUtil.fireErrorMessage("Canno't send message to yourself!");
			logger.error("Error while sending message from profile page, studentId was the same as logged student id : "+loggedUserBean.getUser().getId());
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
