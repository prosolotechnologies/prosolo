package org.prosolo.bigdata.events.observers;

import java.util.Optional;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.impl.UserSessionDBManagerImpl;
import org.prosolo.bigdata.events.analyzers.SequentialSessionAnalyzer;
import org.prosolo.bigdata.events.analyzers.SessionAnalyzer;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

public class UserSessionObserver implements EventObserver {

	private SessionAnalyzer analyzer = new SequentialSessionAnalyzer();

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
			UserSessionDBManagerImpl.getInstance().userSessionStarted(logEvent.getActorId(), logEvent.getTimestamp());
		}
		else {
			Optional<SessionRecord> userSessionEnded = UserSessionDBManagerImpl.getInstance().userSessionEnded(logEvent.getActorId(), logEvent.getTimestamp(),
					logEvent.getEventType().name());
			if(userSessionEnded.isPresent()) {
				analyzer.analyzeSession(userSessionEnded.get());
			}
		}
	}

	public SessionAnalyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(SessionAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
}
