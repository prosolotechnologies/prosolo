package org.prosolo.web.notification;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "topInboxBean")
@Component("topInboxBean")
@Scope("session")
public class TopInboxBean implements Serializable {

	private static final long serialVersionUID = -6523581537208723654L;
	protected static Logger logger = Logger.getLogger(TopInboxBean.class);

	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;
	private List<Long> unreadThreadIds = new ArrayList<>();
	
	@Autowired
	private MessagingManager messagingManager;
	@Autowired
	private LoggedUserBean loggedUser;

	@PostConstruct
	public void checkUnreadMessages() {

		List<MessageThread> unreadThreads = messagingManager.getUnreadMessageThreads(loggedUser.getUser().getId());

		if (CollectionUtils.isNotEmpty(unreadThreads)) {
			for(MessageThread thread : unreadThreads) {
				unreadThreadIds.add(thread.getId());
			}
		}
	}

	public void readMessagesAndRedirect() {
		//simple "action" attribute from command link would not initialize view bean?
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("messages?faces-redirect=true");
		} catch (IOException e) {
			logger.error("Error redirecting", e);
		}
	}
	
	public void markThreadRead(Long id) {
		unreadThreadIds.remove(id);
	}
	
	public void addUnreadThread(Long id) {
		unreadThreadIds.add(id);
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public MessagingManager getMessagingManager() {
		return messagingManager;
	}

	public void setMessagingManager(MessagingManager messagingManager) {
		this.messagingManager = messagingManager;
	}

	public LoggedUserBean getLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}

	public List<Long> getUnreadThreadIds() {
		return unreadThreadIds;
	}

	public void setUnreadThreadIds(List<Long> unreadThreadIds) {
		this.unreadThreadIds = unreadThreadIds;
	}
	
	

}
