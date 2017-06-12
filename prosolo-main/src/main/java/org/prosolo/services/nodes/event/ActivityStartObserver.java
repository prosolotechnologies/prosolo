package org.prosolo.services.nodes.event;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.springframework.stereotype.Service;

@Deprecated
@Service("org.prosolo.services.nodes.event.ActivityStartObserver")
public class ActivityStartObserver extends EventObserver {

	@Inject
	private ActivityStartEventProcessorFactory activityStartEventProcessorFactory;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] {
			EventType.SERVICEUSE,
			EventType.NAVIGATE
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		ActivityStartEventProcessor processor = activityStartEventProcessorFactory
				.getActivityStartEventProcessor(event);
		if(processor != null) {
			processor.updateActivity();
		}
	}

	

}
