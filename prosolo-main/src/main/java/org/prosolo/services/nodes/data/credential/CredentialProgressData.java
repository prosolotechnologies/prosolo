package org.prosolo.services.nodes.data.credential;

import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.web.manage.students.data.CompetenceProgressData;

import java.io.Serializable;
import java.util.List;

public class CredentialProgressData implements Serializable {

    private static final long serialVersionUID = 6996079592796982824L;

    private long id;
    private long credentialId;
    private String name;
    private int progress;
    private List<CompetenceProgressData> competences;
    private CompetenceProgressData selectedCompetence;

    public CredentialProgressData(TargetCredential1 credential) {
        this.id = credential.getId();
        this.credentialId = credential.getCredential().getId();
        this.name = credential.getCredential().getTitle();
        this.progress = credential.getProgress();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCredentialId() {
        return credentialId;
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
