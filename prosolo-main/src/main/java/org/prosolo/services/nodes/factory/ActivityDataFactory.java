package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.services.nodes.data.BasicActivityData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.springframework.stereotype.Component;

@Component
public class ActivityDataFactory {

	public BasicActivityData getActivityData(CompetenceActivity1 competenceActivity, boolean shouldTrackChanges) {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}
		BasicActivityData act = new BasicActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		act.setCompetenceActivityId(competenceActivity.getId());
		act.setActivityId(activity.getId());
		act.setOrder(competenceActivity.getOrder());
		act.setTitle(activity.getTitle());
		act.setDescription(activity.getDescription());
		act.setDuration(activity.getDuration());
		act.setPublished(activity.isPublished());
		act.setActivityStatus();

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}
	
	public BasicActivityData getActivityData(TargetActivity1 activity, boolean shouldTrackChanges) {
		if(activity == null) {
			return null;
		}
		BasicActivityData act = new BasicActivityData(false);
		act.setActivityId(activity.getId());
		act.setOrder(activity.getOrder());
		act.setTitle(activity.getTitle());
		act.setDescription(activity.getDescription());
		act.setDuration(activity.getDuration());

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}
	
//	public BasicActivityData getActivityData(Activity1 activity, boolean shouldTrackChanges) {
//	if(activity == null) {
//		return null;
//	}
//	BasicActivityData act = new BasicActivityData(false);
//	act.setId(activity.getId());
//	act.setOrder(activity.getOrderInCompetence());
//	act.setTitle(activity.getTitle());
//	act.setDescription(activity.getDescription());
//	act.setDuration(activity.getDuration());
//	act.setPublished(activity.isPublished());
//	act.setActivityStatus();
//
//	act.setObjectStatus(ObjectStatus.UP_TO_DATE);
//	
//	if(shouldTrackChanges) {
//		act.startObservingChanges();
//	}
//	
//	return act;
//}
	
}
