package org.prosolo.services.nodes.event;

import java.util.Map;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.ApplicationBean;
import org.springframework.stereotype.Service;

@Deprecated
@Service
public class ActivityStartEventProcessorFactory {
	
	@Inject private ApplicationBean applicationBean;
	@Inject private ActivityManager activityManager;

	public ActivityStartEventProcessor getActivityStartEventProcessor(Event event) {
		EventType type = event.getAction();
		Map<String, String> params = event.getParameters();
		if(type == EventType.NAVIGATE) {
			return new ResourceLinkActivityStartEventProcessor(event, applicationBean, activityManager);
		} else if (type == EventType.SERVICEUSE && params != null && "VIDEO".equals(params.get("objectType"))) {
			return new YoutubeActivityStartEventProcessor(event, applicationBean, activityManager);
		} else {
			return null;
		}
	}
}
