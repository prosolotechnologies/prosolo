package org.prosolo.services.nodes.event;

import java.util.Map;

import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.ApplicationBean;

public class YoutubeActivityStartEventProcessor extends ActivityStartEventProcessor {

	public YoutubeActivityStartEventProcessor(Event event, ApplicationBean applicationBean,
			ActivityManager activityManager) {
		super(event, applicationBean, activityManager);
	}

	@Override
	boolean checkSpecificCondition() {
		Map<String, String> params = event.getParameters();
		String action = params.get("action");
		if("PLAYING".equals(action)) {
			return true;
		}
		
		return false;
	}

}
