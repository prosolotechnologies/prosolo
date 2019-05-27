package org.prosolo.services.data;

import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventData;
import org.prosolo.common.event.EventQueue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Result<T> {

	private T result;
	private List<T> results;
	private EventQueue eventQueue;
	private List<Event> firedEvents;
	
	public Result() {
		results = new LinkedList<>();
		eventQueue = EventQueue.newEventQueue();
		firedEvents = new ArrayList<>();
	}

	public static Result<Void> empty() {
		return new Result<>();
	}

	public static Result<Void> of(EventQueue eventQueue) {
		Result<Void> res = new Result<>();
		res.appendEvents(eventQueue);
		return res;
	}
	
	public void appendEvent(EventData ev) {
		if (ev != null) {
			eventQueue.appendEvent(ev);
		}
	}
	
	public void appendEvents(EventQueue eventQueue) {
		if (eventQueue != null) {
			this.eventQueue.appendEvents(eventQueue);
		}
	}
	
	public void addFiredEvent(Event ev) {
		if(ev != null) {
			firedEvents.add(ev);
		}
	}
	
	public void addResult(T res) {
		results.add(res);
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}

	public List<Event> getFiredEvents() {
		return firedEvents;
	}
	
}
