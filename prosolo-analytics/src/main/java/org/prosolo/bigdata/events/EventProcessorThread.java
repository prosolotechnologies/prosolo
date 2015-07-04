package org.prosolo.bigdata.events;

import org.prosolo.bigdata.events.observers.EventObserver;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.apache.log4j.Logger;

/**
@author Zoran Jeremic Apr 5, 2015
 *
 */

public class EventProcessorThread  extends Thread {
	private EventObserver observer;
	private DefaultEvent event;
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	public EventProcessorThread(EventObserver observer, DefaultEvent event2) {
		this.observer = observer;
		this.event = event2;
	}

	@Override
	public void run() {
		this.processEvent();
	}

	private void processEvent() {
		Topic[] topics = this.observer.getSupportedTopics();
		if (!this.isInTopicsArray(topics, this.event.getTopic())) {
			return;
		}
		if (!this.isInTypesArray(this.observer.getSupportedTypes(),
				this.event.getEventType())) {
			return;
		}
	
		this.observer.handleEvent(this.event);
	}

	private boolean isInTopicsArray(Topic[] supportedTopics, Topic eventTopic) {
		if (supportedTopics == null || supportedTopics.length == 0) {
			return true;
		}
		for (int i = 0; i < supportedTopics.length; i++) {
			if (supportedTopics[i].equals(eventTopic)) {
				return true;
			}

		}
		return false;
	}

	private boolean isInTypesArray(String[] supportedTypes, String eventType) {
		if (supportedTypes == null || supportedTypes.length == 0) {
			return true;
		}
		for (int i = 0; i < supportedTypes.length; i++) {
			if (supportedTypes[i].equals(eventType)) {
				return true;
			}
		}
		return false;
	}

 
 
}

