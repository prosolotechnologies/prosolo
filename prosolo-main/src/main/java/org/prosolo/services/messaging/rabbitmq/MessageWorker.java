package org.prosolo.services.messaging.rabbitmq;

import org.prosolo.services.messaging.rabbitmq.impl.WorkerException;

/**
@author Zoran Jeremic Sep 7, 2014
 */

public interface MessageWorker {
	public void handle(String message) throws WorkerException;
}
