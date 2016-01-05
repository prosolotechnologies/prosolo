package org.prosolo.web.observer;

import java.util.Map;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.services.nodes.event.ResourceLinkActivityStartEventProcessor;
import org.prosolo.services.nodes.event.YoutubeActivityStartEventProcessor;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;

@Service
public class TimeSpentOnActivityProcessorFactory {

	@Inject
	private ActivityManager activityManager;
	
	public TimeSpentOnActivityProcessor getTimeSpentOnActivityProcessor(DBObject event) {
		EventType type = EventType.valueOf((String) event.get("eventType"));
		DBObject params = (DBObject) event.get("parameters");
		if(type == EventType.NAVIGATE) {
			return new LinkTimeSpentOnActivityProcessor(event, activityManager);
		} else if (type == EventType.SERVICEUSE && params != null && "VIDEO".equals(params.get("objectType"))) {
			return new YoutubeTimeSpentOnActivityProcessor(event, activityManager);
		} else {
			return null;
		}
	}
}
