package org.prosolo.services.studentProfile.progression.observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.services.logging.LoggingEventsObserver;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;

@Deprecated
@Service
public class TimeSpentOnActivityObserver extends EventObserver {

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
	
	/* (non-Javadoc)
	 * @see org.prosolo.services.event.EventObserver#getPrerequisiteObservers()
	 *
	 * @version 0.5
	 */
	@Override
	public Collection<Class<? extends EventObserver>> getPrerequisiteObservers() {
		Collection<Class<? extends EventObserver>> prerequisiteClasses = new ArrayList<>();
		
		prerequisiteClasses.add(LoggingEventsObserver.class);
		
		return prerequisiteClasses;
	}

}
