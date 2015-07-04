package org.prosolo.common.domainmodel.activities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.activities.Activity;

@Entity
// //@Table(name="act_Activity")
public class CompetenceActivity extends BaseEntity {

	private static final long serialVersionUID = -3166807366434198727L;

	private long activityPosition;
	private Activity activity;
	
	public CompetenceActivity() {}
	
	public CompetenceActivity(long activityPosition, Activity activity) {
		this.activityPosition = activityPosition;
		this.activity = activity;
	}

	public long getActivityPosition() {
		return activityPosition;
	}

	public void setActivityPosition(long activityPosition) {
		this.activityPosition = activityPosition;
	}

	@OneToOne
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

}
