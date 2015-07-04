package org.prosolo.bigdata.rabbitmq;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.events.EventDispatcher;
import org.prosolo.bigdata.events.EventFactory;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;

/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public class DefaultMessageWorker {
	private final static Logger logger = Logger
			.getLogger(DefaultMessageWorker.class);
	// private static JsonParser parser = new JsonParser();
	private Topic workerTopic=Topic.LOGS;
 	private EventDispatcher eventDispatcher;

	 public DefaultMessageWorker(Topic topic,EventDispatcher eventDispatcher) {
		// gson.registerTypeAdapter(MessageWrapper.class, new
		// MessageWrapperAdapter());
		this.workerTopic = topic;
	 	this.eventDispatcher = eventDispatcher;
	}
 
	public void handle(String message) throws WorkerException {
		logger.debug("WORKER FOR TOPIC:"+this.workerTopic.name()+" HANDLING MESSAGE:" + message);
		DefaultEvent event=null;
		if(this.workerTopic.equals(Topic.LOGS)){
			event=EventFactory.createLogEvent(workerTopic, message);
		}else if(this.workerTopic.equals(Topic.ANALYTICS)){
			event=EventFactory.createAnalyticsEvent(workerTopic, message);
		}
		this.eventDispatcher.dispatchEvent(event);
	}
}

