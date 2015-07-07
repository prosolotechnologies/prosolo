package org.prosolo.services.email.notification;

import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.springframework.stereotype.Service;

@Service("org.prosolo.notification.InstantUpdateEventObserver")
public class InstantUpdateEventObserver implements EventObserver {

	@Override
	public EventType[] getSupportedEvents() {
		EventType[] eventsToHandle=
			{
//				SharingEvent.class, 
				//RecommendationEvent.class
			};
		return eventsToHandle;
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		if(Settings.getInstance().config.emailNotifier.activated){
			//new InstantUpdateThread(event).start(); 
		}
	}
}
