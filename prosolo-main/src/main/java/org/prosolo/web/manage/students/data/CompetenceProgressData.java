package org.prosolo.web.manage.students.data;

import java.io.Serializable;
import java.util.List;

import org.prosolo.common.domainmodel.credential.LearningPathType;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;

public class CompetenceProgressData implements Serializable{

	private static final long serialVersionUID = -2605340288766333695L;

	private long id;
	private long competenceId;
	private String name;
	private int progress;
	private LearningPathType learningPathType;
	private List<LearningEvidenceData> evidences;
	private List<ActivityProgressData> activities;
	
	private long approvedSubmissionNumber;
	private long rejectedSubmissionNumber;
	private boolean trophyWon;
	
	public boolean isTrophyWon() {
		return trophyWon;
	}

	public void setTrophyWon(boolean trophyWon) {
		this.trophyWon = trophyWon;
	}
	
	public CompetenceProgressData() {
		
	}
	
	public CompetenceProgressData(CompetenceData1 compData){
		this.id = compData.getTargetCompId();
		this.competenceId = compData.getCompetenceId();
		this.name = compData.getTitle();
		this.progress = compData.getProgress();
		this.learningPathType = compData.getLearningPathType();
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

	public List<ActivityProgressData> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityProgressData> activities) {
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

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public LearningPathType getLearningPathType() {
		return learningPathType;
	}

	public void setLearningPathType(LearningPathType learningPathType) {
		this.learningPathType = learningPathType;
	}

	public List<LearningEvidenceData> getEvidences() {
		return evidences;
	}

	public void setEvidences(List<LearningEvidenceData> evidences) {
		this.evidences = evidences;
	}
}
