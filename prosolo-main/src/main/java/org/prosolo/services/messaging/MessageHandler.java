package org.prosolo.services.messaging;

import org.prosolo.bigdata.common.rabbitmq.SimpleMessage;

 

public interface MessageHandler<T extends SimpleMessage> {

	public void handle(T message);
 

}