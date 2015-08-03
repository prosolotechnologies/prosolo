package org.prosolo.services.messaging;

import org.prosolo.common.messaging.data.SimpleMessage;

 

public interface MessageHandler<T extends SimpleMessage> {

	public void handle(T message);
 

}