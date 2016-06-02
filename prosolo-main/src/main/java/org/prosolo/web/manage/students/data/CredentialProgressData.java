package org.prosolo.web.manage.students.data;

import java.io.Serializable;
import java.util.List;

import org.prosolo.common.domainmodel.credential.TargetCredential1;

public class CredentialProgressData implements Serializable {

	private static final long serialVersionUID = 6996079592796982824L;
	
	private long id;
	private String name;
	private int progress;
	private List<CompetenceProgressData> competences;
	private CompetenceProgressData selectedCompetence;
	
	public CredentialProgressData(){
		
	}
	
	public CredentialProgressData(TargetCredential1 credential){
		this.id = credential.getId();
		this.name = credential.getTitle();
		this.progress = credential.getProgress();
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

	public List<CompetenceProgressData> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceProgressData> competences) {
		this.competences = competences;
	}

	public CompetenceProgressData getSelectedCompetence() {
		return selectedCompetence;
	}

	public void setSelectedCompetence(CompetenceProgressData selectedCompetence) {
		this.selectedCompetence = selectedCompetence;
	}
	
	
}
