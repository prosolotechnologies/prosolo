package org.prosolo.common.messaging.rabbitmq;

/**
 * @author zoran Aug 2, 2015
 */

public interface ReliableConsumer extends ReliableClient{

	void StartAsynchronousConsumer();
	void StopAsynchronousConsumer();

	void setWorker(MessageWorker worker);

}
