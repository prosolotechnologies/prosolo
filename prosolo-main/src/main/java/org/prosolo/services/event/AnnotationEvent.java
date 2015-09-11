package org.prosolo.services.event;

import org.prosolo.common.domainmodel.activities.events.EventType;

//@Entity
public class AnnotationEvent extends Event {

	private static final long serialVersionUID = 7011314343969505276L;

	public AnnotationEvent() {

	}

	public AnnotationEvent(EventType eventType) {
		super(eventType);
	}

}
