package org.prosolo.services.interaction;

import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.messaging.MessageThread;

/**
 * @author Nikola Milikic
 * @version 0.5
 */
public interface MessageInboxUpdater {

    void updateOnNewMessage(HttpSession httpSession);

}
