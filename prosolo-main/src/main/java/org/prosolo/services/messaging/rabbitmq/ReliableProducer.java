package org.prosolo.services.messaging.rabbitmq;

import org.prosolo.services.messaging.rabbitmq.impl.QueueNames;

/**
 @author Zoran Jeremic Sep 7, 2014
 */

public interface ReliableProducer {

	public abstract void send(String data);

	public abstract void startAsynchronousPublisher();

	public abstract void stopAsynchronousPublisher();

	public abstract void setQueue(String lowerCase);

	//ReliableProducer init(QueueNames queueName);

}