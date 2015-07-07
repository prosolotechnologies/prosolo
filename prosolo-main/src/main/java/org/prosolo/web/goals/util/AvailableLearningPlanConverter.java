package org.prosolo.web.goals.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.LearningPlan;
import org.prosolo.services.nodes.ActivityManager;
import org.prosolo.web.goals.data.AvailableLearningPlan;
import org.prosolo.web.goals.data.RecommendedActivityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.web.goals.util.AvailableLearningPlanConverter")
public class AvailableLearningPlanConverter implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8448014859753390875L;
	@Autowired private ActivityManager activityManager;

	public List<AvailableLearningPlan> packIntoAvailablePlan(User user, LearningPlan currentPlan, List<LearningPlan> recommendLearningPlans) {
		List<AvailableLearningPlan> availablePlans = new ArrayList<AvailableLearningPlan>();

		for (int i = 0; i < recommendLearningPlans.size(); i++) {
			LearningPlan plan = recommendLearningPlans.get(i);
			
			AvailableLearningPlan availableLearningPlan = new AvailableLearningPlan(i + 1, plan);
			
			Collections.sort(plan.getActivities());
			
			for (Activity activity : plan.getActivities()) {
				activity = activityManager.merge(activity);
				RecommendedActivityData actData = new RecommendedActivityData(activity);
				
				if (user != null) {
					actData.setCompletedByUser(activityManager.checkIfCompletedByUser(user, activity));
				}
				
				if (currentPlan != null) {
					//currentPlan = activityManager.merge(currentPlan);
					boolean inCurrentPlan = isActivityInLearningPlan(activity, currentPlan);
					actData.setContainedInCurrentPlan(inCurrentPlan);
				}
				availableLearningPlan.addActivitiy(actData);
			}
			
			availablePlans.add(availableLearningPlan);
		}

		return availablePlans;
	}
	
	private boolean isActivityInLearningPlan(Activity activity, LearningPlan plan) {
		Collection<Activity> activities = plan.getActivities();
		if(activities.contains(activity)){
			return true;
		}else return false;
		
//		for (Activity plActivity : activities) {
//			if (plActivity.equals(activity))
//				return true;
//			
//			Activity tempAct = plActivity;
//			
//			while (tempAct.getBasedOn() != null) {
//				tempAct = tempAct.getBasedOn();
//
//				if (tempAct.equals(activity)) {
//					return true;
//				}
//			}
//		}
	//	return false;
	}
	
//	private void getAllActivitiesBasedOnHierarchy(LearningPlan plan) {
////		Collection<Activity> addedActs = suggLearningQueries.loadAllActivitiesForLearningPlan(selectedComp.getPlan());
//		Collection<Activity> addedActs = plan.getActivities();
//		
//		for (Activity act : addedActs) {
//			addedActivities.add(act.getUri());
//			Activity tempAct = act;
//			
//			while (tempAct.getBasedOn() != null) {
//				addedActivities.add(tempAct.getBasedOn().getUri());
//				tempAct = tempAct.getBasedOn();
//			}
//		}
//	}
}
