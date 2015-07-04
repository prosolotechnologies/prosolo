package org.prosolo.bigdata.events;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.events.observers.EventObserver;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;
/**
@author Zoran Jeremic Apr 5, 2015
 *
 */

public class EventDispatcher {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	EventsDispatcherThreadPoolExecutor executor;
	/* List of properties */
	private Set<EventObserver> observers;

	// EventFactory eventFactory=new EventFactory();

	/* Custom methods */
	public EventDispatcher() {
		this.observers = new HashSet<EventObserver>();
		this.executor = EventsDispatcherThreadPoolExecutor.getInstance();
	}

	public void registerObserver(EventObserver observer) {
		if (!this.observers.contains(observer)) {
			this.observers.add(observer);
		}
	}

	public void dispatchEvent(DefaultEvent event) {
		for (EventObserver observer : this.observers) {
			EventProcessorThread eventProcessor = new EventProcessorThread(
					observer, event);
			this.executor.submit(eventProcessor);
		}
	}



}

