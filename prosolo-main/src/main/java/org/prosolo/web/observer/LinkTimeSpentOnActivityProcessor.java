package org.prosolo.web.observer;

import org.prosolo.services.nodes.ActivityManager;

import com.mongodb.DBObject;

public class LinkTimeSpentOnActivityProcessor extends TimeSpentOnActivityProcessor {

	public LinkTimeSpentOnActivityProcessor(DBObject event, ActivityManager activityManager) {
		super(event, activityManager);
	}

	@Override
	protected boolean checkAdditionalConditions() {
		return true;
	}

	@Override
	protected boolean checkIfEventStoppedLearning(DBObject nextEv) {
		return true;
	}

}
