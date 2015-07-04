package org.prosolo.bigdata.streaming;

import org.prosolo.bigdata.events.EventDispatcher;
import org.prosolo.bigdata.rabbitmq.DefaultMessageWorker;
import org.prosolo.bigdata.rabbitmq.ReliableConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class StreamConsumerManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	 private EventDispatcher eventDispatcher;
	
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
		//this.topics = new HashMap<Topic, Integer>();
		this.setEventDispatcher(new EventDispatcher());
	}
	public void startTopicStreaming(Topic topic, Integer threadCount) {
		this.logger.info("START LISTENING FOR TOPIC:" + topic.name());
 

			ReliableConsumer reliableConsumer = new ReliableConsumer();
			reliableConsumer.setQueue(topic.name().toLowerCase());
			reliableConsumer.setWorker(new DefaultMessageWorker(topic, this.eventDispatcher));
			reliableConsumer.StartAsynchronousConsumer();
		 
	}
	public EventDispatcher getEventDispatcher() {
		return this.eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}
}

