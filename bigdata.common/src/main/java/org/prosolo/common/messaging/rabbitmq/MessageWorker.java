package org.prosolo.common.messaging.rabbitmq;

import org.prosolo.common.messaging.rabbitmq.MessageWorker;

/**
@author Zoran Jeremic Sep 7, 2014
 */

public interface MessageWorker {
	public void handle(String message) throws WorkerException;
}
