package org.prosolo.common.domainmodel.credential;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.prosolo.common.domainmodel.general.BaseEntity;

@Entity
public class TargetActivity1 extends BaseEntity {

	private static final long serialVersionUID = -2861912495505619686L;
	
	private boolean completed;
	private TargetCompetence1 targetCompetence;
	private Activity1 activity;
	
	private Date dateCompleted;
	//is activity added by student
	private boolean added;
	
	//common score for all activity assessments
	private int commonScore = -1;
	private int numberOfAttempts;
	
	//activity result - uploaded file link or textual response
	private String result;
	private Date resultPostDate;
	
	private long timeSpent;
	
    private int order;
	
	public TargetActivity1() {
		
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public TargetCompetence1 getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence1 targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Activity1 getActivity() {
		return activity;
	}

	public void setActivity(Activity1 activity) {
		this.activity = activity;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public long getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(long timeSpent) {
		this.timeSpent = timeSpent;
	}
	
	@Column(length = 90000)
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getResultPostDate() {
		return resultPostDate;
	}

	public void setResultPostDate(Date resultPostDate) {
		this.resultPostDate = resultPostDate;
	}

	public int getCommonScore() {
		return commonScore;
	}

	public void setCommonScore(int commonScore) {
		this.commonScore = commonScore;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}
	
	@Column(name = "actOrder")
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
