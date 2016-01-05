package org.prosolo.common.domainmodel.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;

@Entity
public class TargetActivity extends Node {

	private static final long serialVersionUID = 5698106325669307105L;

	private boolean completed;
	private Date dateStarted;
	private Date dateCompleted;
	private long taPosition;
	private Activity activity;
	private TargetCompetence parentCompetence;

	private TargetActivity basedOn;
	
	// used for UploadAssignmentActivity
	private String assignmentLink;
	private String assignmentTitle;
	private List<Outcome> outcomes;
	
	private long timeSpent;
	
	public TargetActivity(){
		setOutcomes(new ArrayList<Outcome>());
	}
	
	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public long getTaPosition() {
		return taPosition;
	}

	public void setTaPosition(long taPosition) {
		this.taPosition = taPosition;
	}

	@OneToOne
	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	@OneToOne(fetch = FetchType.LAZY)
	public TargetActivity getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(TargetActivity basedOn) {
		this.basedOn = basedOn;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public TargetCompetence getParentCompetence() {
		return parentCompetence;
	}

	public void setParentCompetence(TargetCompetence parentCompetence) {
		this.parentCompetence = parentCompetence;
	}
	
	public String getAssignmentLink() {
		return assignmentLink;
	}

	public void setAssignmentLink(String assignmentLink) {
		this.assignmentLink = assignmentLink;
	}

	public String getAssignmentTitle() {
		return assignmentTitle;
	}

	public void setAssignmentTitle(String assignmentTitle) {
		this.assignmentTitle = assignmentTitle;
	}

	@Override
	public int compareTo(Node node) {
		TargetActivity tActivity = (TargetActivity) node;
		return this.getActivity().compareTo(tActivity.getActivity());
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	public List<Outcome> getOutcomes() {
		return outcomes;
	}

	public void setOutcomes(List<Outcome> outcomes) {
		this.outcomes = outcomes;
	}

	@Column(nullable = false, columnDefinition = "bigint(20) default 0")
	public long getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}
	
}
