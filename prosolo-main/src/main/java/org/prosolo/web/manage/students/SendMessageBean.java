package org.prosolo.web.manage.students;

import org.apache.log4j.Logger;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	private String message;

	public void sendMessage(long receiverId) {
		if(receiverId != loggedUserBean.getUserId()) {
			try {
				messagingManager.sendMessage(0, loggedUserBean.getUserId(),
						receiverId, this.message, loggedUserBean.getUserContext());

				logger.debug("User "+loggedUserBean.getUserId()+" sent a message to " + receiverId +
						" with content: '"+message+"'");
				
				List<UserData> participants = new ArrayList<UserData>();
				
				participants.add(new UserData(loggedUserBean.getUserId(), loggedUserBean.getFullName(), loggedUserBean.getAvatar()));

				this.message = "";

				PageUtil.fireSuccessfulInfoMessage("Your message is sent");
			} catch (Exception e) {
				logger.error(e);
			}
		}
		else {
			PageUtil.fireErrorMessage("Can not send message to yourself!");
			logger.error("Error sending message from profile page, studentId was the same as logged student id : "+loggedUserBean.getUserId());
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
