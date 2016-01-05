package org.prosolo.services.studentProfile.progression.observer;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.services.nodes.ActivityManager;

import com.mongodb.DBObject;

public class YoutubeTimeSpentOnActivityProcessor extends TimeSpentOnActivityProcessor {

	public YoutubeTimeSpentOnActivityProcessor(DBObject event, ActivityManager activityManager) {
		super(event, activityManager);
	}

	@Override
	protected boolean checkAdditionalConditions() {
		DBObject params = (DBObject) event.get("parameters");
		String action = (String) params.get("action");
		if("PLAYING".equals(action)) {
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean checkIfEventStoppedLearning(DBObject nextEv) {
		EventType type = EventType.valueOf((String) nextEv.get("eventType"));
		DBObject params = (DBObject) nextEv.get("parameters");
		
		if(params != null) {
			String objectType = (String) params.get("objectType");
			String action = (String) params.get("action");
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
