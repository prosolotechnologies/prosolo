package org.prosolo.web.students.data.learning;

import java.io.Serializable;
import java.util.List;

import org.prosolo.common.domainmodel.competences.TargetCompetence;

public class CompetenceData implements Serializable{

	private static final long serialVersionUID = -2605340288766333695L;

	private long id;
	private String name;
	private int progress;
	private List<ActivityData> activities;
	
	private long approvedSubmissionNumber;
	private long rejectedSubmissionNumber;
	private boolean trophyWon;
	
	private List<EvaluationSubmissionData> submissions;
	
	public boolean isTrophyWon() {
		return trophyWon;
	}

	public void setTrophyWon(boolean trophyWon) {
		this.trophyWon = trophyWon;
	}
	
	public CompetenceData() {
		
	}
	
	public CompetenceData(TargetCompetence tCompetence){
		this.id = tCompetence.getId();
		this.name = tCompetence.getTitle();
		this.progress = tCompetence.getProgress();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<ActivityData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityData> activities) {
		this.activities = activities;
	}

	public long getApprovedSubmissionNumber() {
		return approvedSubmissionNumber;
	}

	public void setApprovedSubmissionNumber(long approvedSubmissionNumber) {
		this.approvedSubmissionNumber = approvedSubmissionNumber;
	}

	public long getRejectedSubmissionNumber() {
		return rejectedSubmissionNumber;
	}

	public void setRejectedSubmissionNumber(long rejectedSubmissionNumber) {
		this.rejectedSubmissionNumber = rejectedSubmissionNumber;
	}

	public List<EvaluationSubmissionData> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(List<EvaluationSubmissionData> submissions) {
		this.submissions = submissions;
	}
	
	
}
