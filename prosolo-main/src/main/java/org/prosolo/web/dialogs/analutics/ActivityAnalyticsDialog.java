package org.prosolo.web.dialogs.analutics;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.services.logging.ComponentName;
import org.prosolo.services.stats.ActivityStatistics;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.dialogs.analutics.data.ActivityAnalyticsData;
import org.prosolo.web.logging.LoggingNavigationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "activityAnalyticsBean")
@Component("activityAnalyticsBean")
@Scope("view")
public class ActivityAnalyticsDialog implements Serializable {

	private static final long serialVersionUID = -5196142674286328741L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityAnalyticsDialog.class);
	
	@Autowired private LoggedUserBean loggedUser;

	@Autowired private ActivityStatistics activityStatistics;
	@Autowired private LoggingNavigationBean actionLogger;
	
	private Activity activity;
	private ActivityAnalyticsData data = new ActivityAnalyticsData();;
	
	/*
	 * ACTIONS
	 */

	public void initialize(Activity activity, String context) {
		this.activity = activity;
		
		data = new ActivityAnalyticsData();
		data.setLikes(activityStatistics.getNumberOfLikes(activity));
		data.setDislikes(activityStatistics.getNumberOfDislikes(activity));
		data.setOngoingGoals(activityStatistics.getNumberOfOngoingGoalsWithActivity(activity));
		data.setCompletedGoals(activityStatistics.getNumberOfCompletedGoalsWithActivity(activity));
		
		actionLogger.logServiceUse(
				ComponentName.ACTIVITY_ANALYTICS_DIALOG, 
				"context", context,
				"activityId", String.valueOf(activity.getId()));
	}
	
	/*
	 * GETTERS / SETTERS
	 */

	public Activity getActivity() {
		return activity;
	}

	public ActivityAnalyticsData getData() {
		return data;
	}

}
