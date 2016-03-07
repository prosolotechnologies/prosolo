package org.prosolo.services.messaging;

import org.prosolo.common.messaging.data.SimpleMessage;
import org.prosolo.common.messaging.rabbitmq.WorkerException;


public interface MessageHandler<T extends SimpleMessage> {

	public void handle(T message) throws WorkerException;
 

}