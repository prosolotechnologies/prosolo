package org.prosolo.web.notification;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;

@ManagedBean(name = "topInboxBean")
@Component("topInboxBean")
@Scope("session")
public class TopInboxBean implements Serializable {

	private static final long serialVersionUID = -6523581537208723654L;
	protected static Logger logger = Logger.getLogger(TopInboxBean.class);

	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;

	@Autowired
	private MessagingManager messagingManager;
	@Autowired
	private LoggedUserBean loggedUser;

	private boolean hasUnreadMessages;

	@PostConstruct
	public void checkUnreadMessages() {
		this.hasUnreadMessages = messagingManager.userHasUnreadMessages(loggedUser.getUserId());
	}

	public void markMessageRead() {
		hasUnreadMessages = false;
	}

	public void markMessageUnread() {
		hasUnreadMessages = true;
	}

	public boolean isHasUnreadMessages() {
		return hasUnreadMessages;
	}

	public int getRefreshRate() {
		return refreshRate;
	}
}
