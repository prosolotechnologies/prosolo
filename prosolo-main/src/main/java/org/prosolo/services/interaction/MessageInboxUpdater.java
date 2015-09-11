package org.prosolo.services.interaction;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.user.MessagesThread;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public interface MessageInboxUpdater {

	void updateOnNewMessage(SimpleOfflineMessage message, MessagesThread messagesThread, HttpSession httpSession);

	void addNewMessageThread(MessagesThread messagesThread, HttpSession httpSession);

}
