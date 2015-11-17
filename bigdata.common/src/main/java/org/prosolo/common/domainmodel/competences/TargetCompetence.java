package org.prosolo.common.domainmodel.competences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;

@Entity
public class TargetCompetence extends Node {

	private static final long serialVersionUID = -6912540727611376132L;
	
	private Date completedDay;
	private Competence competence;
	private TargetLearningGoal parentGoal;
	private boolean completed;
	private boolean hideActivityRecommendations;
	
	private Date dateStarted;
	private Date dateFinished;
	private List<TargetActivity> targetActivities;
	
	private TargetCompetence basedOn;
	
	private int progress;
	
	public TargetCompetence() {
		targetActivities = new ArrayList<TargetActivity>();
	}
	
	@Override
	public void setDateCreated(Date dateCreated) {
		super.setDateCreated(dateCreated);
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "completedDay", length = 19)
	public Date getCompletedDay() {
		return completedDay;
	}

	public void setCompletedDay(Date completedDay) {
		this.completedDay = completedDay;
	}

	@OneToOne
	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}
	
	@Override
	public int compareTo(Node o) {
		if (o instanceof TargetCompetence) {
			TargetCompetence tc = (TargetCompetence) o;
			
			return this.competence.compareTo(tc.getCompetence());
		}
		return super.compareTo(o);
	}

	@OneToOne(fetch = FetchType.LAZY)
	public TargetLearningGoal getParentGoal() {
		return parentGoal;
	}

	public void setParentGoal(TargetLearningGoal parentGoal) {
		this.parentGoal = parentGoal;
	}

	@Type(type = "true_false")
	@Column(name="compl", columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isHideActivityRecommendations() {
		return hideActivityRecommendations;
	}

	public void setHideActivityRecommendations(boolean hideActivityRecommendations) {
		this.hideActivityRecommendations = hideActivityRecommendations;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dateStarted", length = 19)
	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		if (dateStarted != null) {
			this.dateStarted = dateStarted;
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dateFinished", length = 19)
	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		if (dateFinished != null) {
			this.dateFinished = dateFinished;
		}
	}
	
	@ManyToMany
	@Cascade({CascadeType.MERGE, CascadeType.REFRESH})
	@OrderBy("taPosition ASC")
	public List<TargetActivity> getTargetActivities() {
		return targetActivities;
	}

	public void setTargetActivities(List<TargetActivity> targetActivities) {
		if (targetActivities != null) {
			this.targetActivities = (List<TargetActivity>) targetActivities;
		}
	}

	public void addTargetActivity(TargetActivity targetActivity) {
		if (null != targetActivity) {
			if (!getTargetActivities().contains(targetActivity)) {
				getTargetActivities().add(targetActivity);
			}
		}
	}
	
	public boolean removeActivity(TargetActivity targetActivity) {
		if (targetActivity != null) {
			return getTargetActivities().remove(targetActivity);
		}
		return false;
	}

	@OneToOne (fetch=FetchType.LAZY)
	public TargetCompetence getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(TargetCompetence basedOn) {
		this.basedOn = basedOn;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@Override
	public String toString() {
		return "TargetCompetence [completedDay=" + completedDay
				+ ", competence=" + competence + ", parentGoal=" + parentGoal
				+ ", completed=" + completed + ", dateStarted=" + dateStarted
				+ ", dateFinished=" + dateFinished + ", basedOn=" + basedOn
				+ ", getTitle()=" + getTitle() + ", getId()=" + getId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((basedOn == null) ? 0 : basedOn.hashCode());
		result = prime * result
				+ ((competence == null) ? 0 : competence.hashCode());
		result = prime * result + (completed ? 1231 : 1237);
		result = prime * result
				+ ((completedDay == null) ? 0 : completedDay.hashCode());
		result = prime * result
				+ ((dateFinished == null) ? 0 : dateFinished.hashCode());
		result = prime * result
				+ ((dateStarted == null) ? 0 : dateStarted.hashCode());
		result = prime * result
				+ ((parentGoal == null) ? 0 : parentGoal.hashCode());
		result = prime
				* result
				+ ((targetActivities == null) ? 0 : targetActivities.hashCode());
		return result;
	}

}
