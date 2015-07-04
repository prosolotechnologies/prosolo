package org.prosolo.domainmodel.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.LearningPlan;

//@Entity
////@Table(name = "wf_LearningPlan")
public class LearningPlan implements Serializable {

	private static final long serialVersionUID = 8609814390300187345L;

	private long id;
	private Date dateStarted;
	private Date dateFinished;
	private List<Activity> activities;
	private List<Activity> appendedActivities;
	private TargetCompetence targetCompetence;
	private User maker;

	private LearningPlan basedOn;

	public LearningPlan() {
		this.activities = new ArrayList<Activity>();
		this.appendedActivities = new ArrayList<Activity>();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "dateStarted", length = 19)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		if (dateStarted != null) {
			this.dateStarted = dateStarted;
		}
	}

	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "dateFinished", length = 19)
	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		if (dateFinished != null) {
			this.dateFinished = dateFinished;
		}
	}

	//@ManyToMany
	//@Cascade({CascadeType.MERGE, CascadeType.REFRESH})
	//@JoinTable(name = "wf_LearningPlan_Activity")
	public List<Activity> getActivities() {
		return activities;
	}

	@SuppressWarnings("unchecked")
	public void setActivities(Collection<? extends Activity> activities) {
		if (activities != null) {
			this.activities = (List<Activity>) activities;
		}
	}

	public void addActivity(Activity activity) {
		if (null != activity) {
			if (!getActivities().contains(activity)) {
				getActivities().add(activity);
			}
		}
	}
	
	public boolean removeActivity(Activity activity) {
		if (activity != null) {
			return getActivities().remove(activity);
		}
		return false;
	}

//	@OneToOne(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.MERGE})
	public LearningPlan getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(LearningPlan basedOn) {
		this.basedOn = basedOn;
	}

	public TargetCompetence getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		this.maker = maker;
	}

	public List<Activity> getAppendedActivities() {
		return appendedActivities;
	}

	public void setAppendedActivities(List<Activity> appendedActivities) {
		this.appendedActivities = appendedActivities;
	}
	public void addAppendedActivity(Activity activity) {
		if (null != activity) {
			if (!getAppendedActivities().contains(activity)) {
				getAppendedActivities().add(activity);
			}
		}
	}
	
	public boolean removeAppendedActivity(Activity activity) {
		if (activity != null) {
			return getAppendedActivities().remove(activity);
		}
		return false;
	}
	
}
