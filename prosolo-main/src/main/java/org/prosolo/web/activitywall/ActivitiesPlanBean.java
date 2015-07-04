package org.prosolo.web.activitywall;

import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.bean.ManagedBean;

import org.prosolo.web.activitywall.data.ActivityWallData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activitiesplanbean")
@Component("activitiesplanbean")
@Scope("view")
public class ActivitiesPlanBean {
	private ArrayList<Long> expandedActivities = new ArrayList<Long>();

	public void showCommentsListener(ActivityWallData activityData) {
		if (expandedActivities.contains(activityData.getObject().getId())) {
			Iterator<Long> iterator = expandedActivities.iterator();
			
			while (iterator.hasNext()) {
				Long id = (Long) iterator.next();
				
				if (id == activityData.getObject().getId()) {
					iterator.remove();
					break;
				}
			}
		} else {
			expandedActivities.add(activityData.getObject().getId());
		}
	}

	public boolean ifCommentsShouldShow(ActivityWallData activityData) {
		return expandedActivities.contains(activityData.getObject().getId());
	}
	 
}
