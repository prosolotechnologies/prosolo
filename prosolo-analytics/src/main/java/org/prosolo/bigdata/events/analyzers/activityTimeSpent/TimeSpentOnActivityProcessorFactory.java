package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import com.google.gson.JsonObject;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.utils.JsonUtil;
import org.prosolo.common.domainmodel.events.EventType;

import java.util.Optional;

public class TimeSpentOnActivityProcessorFactory {
	
	public static Optional<TimeSpentOnActivityProcessor> getTimeSpentOnActivityProcessor(LogEvent event) {
		EventType type = event.getEventType();
		JsonObject params = event.getParameters();
		if(type == EventType.NAVIGATE) {
			return Optional.of(new LinkTimeSpentOnActivityProcessor(event));
		} else if (type == EventType.SERVICEUSE && params != null && "VIDEO".equals(
				JsonUtil.getAsString(params, "objectType"))) {
			return Optional.of(new YoutubeTimeSpentOnActivityProcessor(event));
		} else {
			return Optional.empty();
		}
	}
}
