package org.prosolo.web.notification;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.services.authentication.annotations.AuthenticationChangeType;
import org.prosolo.services.authentication.annotations.SessionAttributeScope;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "topInboxBean")
@Component("topInboxBean")
@Scope("session")
@SessionAttributeScope(end = AuthenticationChangeType.USER_AUTHENTICATION_CHANGE)
public class TopInboxBean implements Serializable {

	private static final long serialVersionUID = -6523581537208723654L;
	protected static Logger logger = Logger.getLogger(TopInboxBean.class);

	/*
	store user id to make sure this bean is in sync with user currently logged in.

	There is a small possibility for this bean to be out of sync when user makes two parallel
	requests (one of them being reauthentication request: LTI, Login as) where with some unlucky timing this bean
	could hold values for previously authenticated user but this is only theoretical possibility
	and will probably never happen in practice.
	 */
	private long userId;
	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;
	private List<Long> unreadThreadIds;
	
	@Autowired
	private MessagingManager messagingManager;
	@Autowired
	private LoggedUserBean loggedUser;

	@PostConstruct
	public void checkUnreadMessages() {
		this.userId = loggedUser.getUserId();
		this.unreadThreadIds =  new ArrayList<>();
		List<MessageThread> unreadThreads = messagingManager.getUnreadMessageThreads(this.userId);

		if (CollectionUtils.isNotEmpty(unreadThreads)) {
			for(MessageThread thread : unreadThreads) {
				unreadThreadIds.add(thread.getId());
			}
		}
	}

	private void refreshDataIfNotInSync() {
		if (loggedUser.getUserId() != this.userId) {
			checkUnreadMessages();
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
		refreshDataIfNotInSync();
		return unreadThreadIds;
	}

	public void setUnreadThreadIds(List<Long> unreadThreadIds) {
		this.unreadThreadIds = unreadThreadIds;
	}
	
	

}
