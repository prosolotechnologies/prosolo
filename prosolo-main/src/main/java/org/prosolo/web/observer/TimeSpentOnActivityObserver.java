package org.prosolo.web.observer;

import java.util.List;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.logging.LoggingDBManager;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;

@Service("org.prosolo.web.observer.TimeSpentOnActivityObserver")
public class TimeSpentOnActivityObserver implements EventObserver {

	@Inject
	private LoggingDBManager loggingDBManager;
	@Inject
	private TimeSpentOnActivityProcessorFactory timeSpentFactory;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] {
			EventType.SESSIONENDED
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		User user = event.getActor();
		if(user != null) {
			List<DBObject> events = loggingDBManager.getAllEventsFromUserSession(user.getId());
			int counter = 0;
			for(DBObject ev : events) {
				TimeSpentOnActivityProcessor processor = timeSpentFactory.getTimeSpentOnActivityProcessor(ev);
				if(processor != null) {
					processor.updateTime(counter, events);
				}
				counter++;
			}
		}
		
	}

}
