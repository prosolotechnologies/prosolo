package org.prosolo.web.students.data.learning;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.services.nodes.CompetenceManager;

public class LearningGoalData implements Serializable {

	private static final long serialVersionUID = 6996079592796982824L;
	
	private long id;
	private String name;
	private int progress;
	private List<CompetenceData> competences;
	private CompetenceData selectedCompetence;
	
	public LearningGoalData(){
		
	}
	
	public LearningGoalData(TargetLearningGoal tGoal){
		this.id = tGoal.getId();
		this.name = tGoal.getTitle();
		this.progress = tGoal.getProgress();
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

	public List<CompetenceData> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceData> competences) {
		this.competences = competences;
	}

	public CompetenceData getSelectedCompetence() {
		return selectedCompetence;
	}

	public void setSelectedCompetence(CompetenceData selectedCompetence) {
		this.selectedCompetence = selectedCompetence;
	}
	
	
}
