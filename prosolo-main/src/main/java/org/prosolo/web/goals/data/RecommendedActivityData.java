/**
 * 
 */
package org.prosolo.web.goals.data;

import java.io.Serializable;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.web.util.nodes.ActivityUtil;

/**
 * @author "Nikola Milikic"
 * 
 */
public class RecommendedActivityData implements Serializable {

	private static final long serialVersionUID = -2518277963029449724L;

	private String title;
	private String uri;
	private long activityId;
	private boolean completedByUser;
	private boolean containedInCurrentPlan;
	private TargetActivity targetActivity;
	private Activity activity;

	public RecommendedActivityData(TargetActivity tActivity) {
		this.title = ActivityUtil.getActivityTitle(tActivity.getActivity());
		// this.uri = tActivity.getUri();
		this.targetActivity = tActivity;
		this.activity = tActivity.getActivity();
		this.activityId = this.activity.getId();
	}

	public RecommendedActivityData(Activity activity) {
		this.title = ActivityUtil.getActivityTitle(activity);
		this.activity = activity;
		this.activityId = activity.getId();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isCompletedByUser() {
		return completedByUser;
	}

	public void setCompletedByUser(boolean completedByUser) {
		this.completedByUser = completedByUser;
	}

	public TargetActivity getTargetActivity() {
		return targetActivity;
	}

	public void setTargetActivity(TargetActivity targetActivity) {
		this.targetActivity = targetActivity;
	}

	public boolean isContainedInCurrentPlan() {
		return containedInCurrentPlan;
	}

	public void setContainedInCurrentPlan(boolean containedInCurrentPlan) {
		this.containedInCurrentPlan = containedInCurrentPlan;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

}
