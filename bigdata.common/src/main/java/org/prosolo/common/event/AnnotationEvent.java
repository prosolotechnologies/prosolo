package org.prosolo.common.event;

import org.prosolo.common.domainmodel.events.EventType;

//@Entity
public class AnnotationEvent extends Event {

	private static final long serialVersionUID = 7011314343969505276L;

	public AnnotationEvent() {

	}

	public AnnotationEvent(EventType eventType) {
		super(eventType);
	}

}
