package org.prosolo.services.messaging.rabbitmq;



public interface ReliableConsumer {

	public abstract void StartAsynchronousConsumer();

	public abstract void StopAsynchronousConsumer();

	public abstract void setQueue(String queue);

	//void init();

	//void init(QueueNames queueName);

}