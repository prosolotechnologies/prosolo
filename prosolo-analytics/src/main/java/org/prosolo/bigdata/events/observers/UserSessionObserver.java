package org.prosolo.bigdata.events.observers;

/**
 * @author Nikola Maric
 *
 */
import org.prosolo.bigdata.dal.cassandra.UserSessionDBManager;
import org.prosolo.bigdata.dal.cassandra.impl.UserSessionDBManagerImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

public class UserSessionObserver implements EventObserver {
	
	private UserSessionDBManager userSesseionDBManager = new UserSessionDBManagerImpl();

	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[]{Topic.LOGS};
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[]{EventType.SESSIONENDED,EventType.LOGIN};
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		LogEvent logEvent = (LogEvent) event;
		if(logEvent.getEventType().equals(EventType.LOGIN)) {
			userSesseionDBManager.userSessionStarted(logEvent.getActorId(), logEvent.getTimestamp());
		}
		else {
			userSesseionDBManager.userSessionEnded(logEvent.getActorId(), logEvent.getTimestamp(),
					logEvent.getEventType().name());
		}
	}

}
