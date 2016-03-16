package org.prosolo.bigdata.events.analyzers.activityTimeSpent;

import org.prosolo.bigdata.events.pojo.LogEvent;

public class LinkTimeSpentOnActivityProcessor extends TimeSpentOnActivityProcessor {

	public LinkTimeSpentOnActivityProcessor(LogEvent event) {
		super(event);
	}

	@Override
	protected boolean checkAdditionalConditions() {
		return true;
	}

	@Override
	protected boolean checkIfStoppedLearning(LogEvent nextEv) {
		return true;
	}

}
