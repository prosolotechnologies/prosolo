package org.prosolo.web.notification;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
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
import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
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

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private MessagingManager messagingManager;

	/*
	store user id to make sure this bean is in sync with user currently logged in.

	There is a small possibility for this bean to be out of sync when user makes two parallel
	requests (one of them being reauthentication request: LTI, Login as) where with some unlucky timing this bean
	could hold values for previously authenticated user but this is only theoretical possibility
	and will probably never happen in practice.
	 */
	private long userId;
	private boolean hasUnreadMessages;
	private int refreshRate = Settings.getInstance().config.application.messagesInboxRefreshRate;

	@PostConstruct
	public void checkUnreadMessages() {
		this.userId = loggedUser.getUserId();
		this.hasUnreadMessages = messagingManager.hasUserUnreadMessages(this.userId);
	}

	private void refreshDataIfNotInSync() {
		if (loggedUser.getUserId() != this.userId) {
			checkUnreadMessages();
		}
	}

	public void markMessageRead() {
		hasUnreadMessages = false;
	}

	public void markMessageUnread() {
		hasUnreadMessages = true;
	}

	public boolean isHasUnreadMessages() {
		refreshDataIfNotInSync();
		return hasUnreadMessages;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

}
