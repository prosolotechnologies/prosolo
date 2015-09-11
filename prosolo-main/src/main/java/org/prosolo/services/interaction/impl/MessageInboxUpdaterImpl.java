package org.prosolo.services.interaction.impl;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.services.interaction.MessageInboxUpdater;
import org.prosolo.web.MessagesBean;
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
	public void updateOnNewMessage(SimpleOfflineMessage message, MessagesThread messagesThread, HttpSession httpSession) {
		if (httpSession != null) {
			TopInboxBean inboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
			
			if (inboxBean != null) {
				inboxBean.updateMessageThread(messagesThread);
			}
			
			MessagesBean messagesBean = (MessagesBean) httpSession.getAttribute("messagesBean");
			
			if (messagesBean != null) {
				
				if (messagesBean.getThreadData().getId() == messagesThread.getId()) {
					messagesBean.addMessage(message);
				}
			}
		}
	}
	
	@Override
	public void addNewMessageThread(MessagesThread messagesThread, HttpSession httpSession) {
		if (httpSession != null) {
			TopInboxBean topInboxBean = (TopInboxBean) httpSession.getAttribute("topInboxBean");
			
			if (topInboxBean != null) {
				topInboxBean.addNewMessageThread(messagesThread);
			}
		}
	}
	
}
