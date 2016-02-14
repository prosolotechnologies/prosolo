package org.prosolo.bigdata.events.observers;

import java.util.List;
import java.util.Optional;

import org.prosolo.bigdata.common.dal.pojo.SessionRecord;
import org.prosolo.bigdata.dal.cassandra.impl.LogEventDBManagerImpl;
import org.prosolo.bigdata.dal.cassandra.impl.UserSessionDBManagerImpl;
import org.prosolo.bigdata.events.analyzers.SequentialSessionAnalyzer;
import org.prosolo.bigdata.events.analyzers.SessionAnalyzer;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

public class UserSessionObserver implements EventObserver {

	private SessionAnalyzer<LogEvent> analyzer = new SequentialSessionAnalyzer();

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
				SessionRecord sessionEndRecord = userSessionEnded.get();
				List<LogEvent> logEventsBetweenTimestamps = LogEventDBManagerImpl.getInstance()
					.getLogEventsBetweenTimestamps(sessionEndRecord.getUserId(), sessionEndRecord.getSessionStart(), sessionEndRecord.getSessionEnd());
				analyzer.analyzeSession(logEventsBetweenTimestamps,sessionEndRecord);
			}
		}
	}

	public SessionAnalyzer<LogEvent> getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(SessionAnalyzer<LogEvent> analyzer) {
		this.analyzer = analyzer;
	}
	
}
