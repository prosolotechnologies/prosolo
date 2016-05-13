package org.prosolo.services.nodes.observers.learningResources;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.nodes.Activity1Manager;

public class ActivityChangeProcessor implements LearningResourceChangeProcessor {

	private BaseEntity activity;
	private ActivityChangeTracker changeTracker;
	private Activity1Manager activityManager;
	
	public ActivityChangeProcessor(BaseEntity activity, ActivityChangeTracker changeTracker, 
			Activity1Manager activityManager) {
		this.activity = activity;
		this.changeTracker = changeTracker;
		this.activityManager = activityManager;
	}

	@Override
	public void process() {
		activityManager.updateTargetActivitiesWithChangedData(activity.getId(), changeTracker);
	}

}
