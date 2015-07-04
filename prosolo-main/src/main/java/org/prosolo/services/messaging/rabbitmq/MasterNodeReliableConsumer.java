package org.prosolo.services.messaging.rabbitmq;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */

public interface MasterNodeReliableConsumer {
	public abstract void StartAsynchronousConsumer();

	public abstract void StopAsynchronousConsumer();

	void init();
}
