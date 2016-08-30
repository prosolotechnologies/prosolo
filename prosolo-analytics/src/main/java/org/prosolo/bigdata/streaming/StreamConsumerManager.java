package org.prosolo.bigdata.streaming;

import org.prosolo.bigdata.events.EventDispatcher;
import org.prosolo.bigdata.rabbitmq.DefaultMessageWorker;
import org.prosolo.common.messaging.rabbitmq.impl.ReliableConsumerImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public class StreamConsumerManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private EventDispatcher eventDispatcher;
	private Map<Topic,ReliableConsumerImpl> reliableConsumers=new HashMap();

	public static class StreamingManagerHolder {
		public static final StreamConsumerManager INSTANCE = new StreamConsumerManager();
	}

	public static StreamConsumerManager getInstance() {
		return StreamingManagerHolder.INSTANCE;
	}

	/**
	 * Constructor is set to private as this class is used as Singleton only
	 */
	private StreamConsumerManager() {
		this.setEventDispatcher(new EventDispatcher());
	}

	public void startTopicStreaming(Topic topic, Integer threadCount) {
		this.logger.info("START LISTENING FOR TOPIC:" + topic.name());
		ReliableConsumerImpl reliableConsumer = new ReliableConsumerImpl();
		reliableConsumer.setQueue(topic.name().toLowerCase());
		reliableConsumer.setWorker(new DefaultMessageWorker(topic,
				this.eventDispatcher));
		reliableConsumer.StartAsynchronousConsumer();
		reliableConsumers.put(topic,reliableConsumer);

	}
	public void stopStreaming(Topic topic){
		reliableConsumers.get(topic).StopAsynchronousConsumer();
	}

	public EventDispatcher getEventDispatcher() {
		return this.eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}
}
