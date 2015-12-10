package org.prosolo.services.interfaceSettings.eventProcessors;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;

public abstract class InterfaceEventProcessor {

	protected Session session;
	protected Event event;
	protected BaseEntity object;
	
	private InterfaceEventProcessor nextProcessor;
	
	public InterfaceEventProcessor(Session session, Event event, BaseEntity object) {
		this.session = session;
		this.event = event;
		this.object = object;
	}

	public void setNextProcessor(InterfaceEventProcessor nextProcessor) {
		this.nextProcessor = nextProcessor;
	}
	
	public void processEvent() {
		process();
		if(nextProcessor != null) {
			nextProcessor.process();
		}
	}
	
	abstract void process();
}
