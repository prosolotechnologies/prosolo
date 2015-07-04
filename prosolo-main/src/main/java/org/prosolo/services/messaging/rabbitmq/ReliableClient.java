package org.prosolo.services.messaging.rabbitmq;

/**
 @author Zoran Jeremic Sep 7, 2014
 */

public interface ReliableClient {

	String getQueue();

	void setQueue(String queue);

}