package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.utils.JsonUtil;
import org.prosolo.common.domainmodel.events.EventType;

import com.google.gson.JsonObject;

public class YoutubeTimeSpentOnActivityProcessor extends TimeSpentOnActivityProcessor {

	public YoutubeTimeSpentOnActivityProcessor(LogEvent event) {
		super(event);
	}

	@Override
	protected boolean checkAdditionalConditions() {
		JsonObject params = event.getParameters();
		String action = params != null ? JsonUtil.getAsString(params, "action") : null;
		if("PLAYING".equals(action)) {
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean checkIfStoppedLearning(LogEvent nextEv) {
		EventType type = nextEv.getEventType();
		JsonObject params = nextEv.getParameters();
		
		if(params != null) {
			String objectType = JsonUtil.getAsString(params, "objectType");
			String action = JsonUtil.getAsString(params, "action");
			if (type == EventType.SERVICEUSE 
					&& "VIDEO".equals(objectType)
					&& ("ENDED".equals(action) 
							|| "PAUSE".equals(action))) {
				return true;
			}
		}
		return false;
	}

}
