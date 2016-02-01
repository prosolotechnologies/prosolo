package org.prosolo.bigdata.session;

import org.prosolo.bigdata.events.pojo.DefaultEvent;

public interface EventMatchCounter<T extends DefaultEvent> {
	
	public void processEvent(T event);
	
	public int getMatchCount();
	
}