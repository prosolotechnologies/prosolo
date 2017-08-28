package org.prosolo.services.data;

import java.util.ArrayList;
import java.util.List;

import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventData;

public class Result<T> {

	private T result;
	private List<T> results;
	private List<EventData> events;
	private List<Event> firedEvents;
	
	public Result() {
		results = new ArrayList<>();
		events = new ArrayList<>();
		firedEvents = new ArrayList<>();
	}

	public Result(T result, List<T> results, List<EventData> events) {
		super();
		this.result = result;
		this.results = results;
		this.events = events;
	}

	public static Result<Void> empty() {
		return new Result<>();
	}

	public static Result<Void> of(List<EventData> events) {
		Result<Void> res = new Result<>();
		res.addEvents(events);
		return res;
	}
	
	public void addEvent(EventData ev) {
		if(ev != null) {
			events.add(ev);
		}
	}
	
	public void addEvents(List<EventData> events) {
		if(events != null) {
			this.events.addAll(events);
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

	public List<EventData> getEvents() {
		return events;
	}

	public void setEvents(List<EventData> events) {
		this.events = events;
	}

	public List<Event> getFiredEvents() {
		return firedEvents;
	}
	
}
