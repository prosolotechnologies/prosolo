package org.prosolo.bigdata.events.observers;

import org.prosolo.bigdata.events.analyzers.UserActionAnalyzer;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public class UserActionsAnalyzerObserver implements EventObserver{

	UserActionAnalyzer actionAnalyzer=new UserActionAnalyzer();
	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[] { EventType.LOGIN, EventType.NAVIGATE, EventType.SERVICEUSE, EventType.ENROLL_COURSE,
				EventType.Create, EventType.FILTER_CHANGE};
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		actionAnalyzer.analyzeLogEvent((LogEvent) event);
	}

}
