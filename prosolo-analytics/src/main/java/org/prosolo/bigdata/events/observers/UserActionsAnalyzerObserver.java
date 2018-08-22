package org.prosolo.bigdata.events.observers;

//import org.prosolo.bigdata.events.analyzers.UserActionAnalyzer;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.events.EventType;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
@Deprecated
public class UserActionsAnalyzerObserver implements EventObserver{

	//UserActionAnalyzer actionAnalyzer=new UserActionAnalyzer();
	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[] { Topic.LOGS };
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[] { EventType.LOGIN, EventType.NAVIGATE, EventType.SERVICEUSE, EventType.ENROLL_COURSE,
				EventType.Create, EventType.FILTER_CHANGE, EventType.Comment, EventType.AddNote, 
				EventType.Completion, EventType.Like, EventType.Post, EventType.PostShare, EventType.Follow, EventType.JOIN_GOAL_REQUEST, EventType.JOIN_GOAL_INVITATION,
				EventType.SEND_MESSAGE, EventType.SELECT_COMPETENCE, EventType.SELECT_GOAL};
	}

	@Override
	public void handleEvent(DefaultEvent event) {

		//actionAnalyzer.analyzeLogEvent((LogEvent) event);
	}

}
