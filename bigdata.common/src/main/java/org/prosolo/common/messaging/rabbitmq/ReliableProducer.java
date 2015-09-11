package org.prosolo.common.messaging.rabbitmq;

/**
 * @author zoran Aug 2, 2015
 */

public interface ReliableProducer extends ReliableClient {

	void startAsynchronousPublisher();

	void send(String data);

	void init();

}
