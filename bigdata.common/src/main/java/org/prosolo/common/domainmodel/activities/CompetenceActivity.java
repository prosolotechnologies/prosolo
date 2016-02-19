package org.prosolo.common.domainmodel.activities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.competences.Competence;

@Entity
// //@Table(name="act_Activity")
public class CompetenceActivity extends BaseEntity {

	private static final long serialVersionUID = -3166807366434198727L;

	private Competence competence;
	private long activityPosition;
	private Activity activity;
	
	public CompetenceActivity() {}
	
	public CompetenceActivity(Competence competence, long activityPosition, Activity activity) {
		this.competence = competence;
		this.activityPosition = activityPosition;
		this.activity = activity;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
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
