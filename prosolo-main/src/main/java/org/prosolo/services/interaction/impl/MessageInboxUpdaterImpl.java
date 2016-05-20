package org.prosolo.services.interaction.impl;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.web.messaging.MessagesBean;
import org.prosolo.web.notification.TopInboxBean;
import org.springframework.stereotype.Service;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
@Service("org.prosolo.services.interaction.MessageInboxUpdater")
public class MessageInboxUpdaterImpl implements MessageInboxUpdater, Serializable {

	private static final long serialVersionUID = 344209605228225877L;

	@Override
	public void updateOnNewMessage(Message message, MessageThread messagesThread, HttpSession httpSession) {
		if (httpSession != null) {
			TopInboxBean topInboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
			topInboxBean.addUnreadThread(messagesThread.getId());
		}
	}


	@Override
	public void addNewMessageThread(MessageThread messagesThread, HttpSession httpSession) {
		if (httpSession != null) {
			TopInboxBean topInboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
			topInboxBean.addUnreadThread(messagesThread.getId());
		}
	}

}
