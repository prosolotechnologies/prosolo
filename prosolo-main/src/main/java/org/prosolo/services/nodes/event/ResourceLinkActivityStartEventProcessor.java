package org.prosolo.services.nodes.event;

import org.prosolo.services.event.Event;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.ApplicationBean;

public class ResourceLinkActivityStartEventProcessor extends ActivityStartEventProcessor {

	
	public ResourceLinkActivityStartEventProcessor(Event event, ApplicationBean applicationBean,
			ActivityManager activityManager) {
		super(event, applicationBean, activityManager);
	}

	@Override
	boolean checkSpecificCondition() {
		return true;
	}

}
