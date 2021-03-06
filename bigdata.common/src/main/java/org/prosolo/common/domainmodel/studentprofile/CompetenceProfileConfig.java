package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.credential.TargetCompetence1;

import javax.persistence.*;
import java.util.Set;

/**
 * Represents competence to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("2")
public class CompetenceProfileConfig extends StudentProfileConfig {

    private static final long serialVersionUID = -7908932205612379209L;

    private CredentialProfileConfig credentialProfileConfig;
    private TargetCompetence1 targetCompetence;

    private Set<CompetenceEvidenceProfileConfig> evidenceProfileConfigs;
    private Set<CompetenceAssessmentProfileConfig> competenceAssessmentProfileConfigs;

    @ManyToOne (fetch = FetchType.LAZY)
    public CredentialProfileConfig getCredentialProfileConfig() {
        return credentialProfileConfig;
    }

    public void setCredentialProfileConfig(CredentialProfileConfig credentialProfileConfig) {
        this.credentialProfileConfig = credentialProfileConfig;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public TargetCompetence1 getTargetCompetence() {
        return targetCompetence;
    }

    public void setTargetCompetence(TargetCompetence1 targetCompetence) {
        this.targetCompetence = targetCompetence;
    }

    @OneToMany(mappedBy = "competenceProfileConfig")
    public Set<CompetenceEvidenceProfileConfig> getEvidenceProfileConfigs() {
        return evidenceProfileConfigs;
    }

    public void setEvidenceProfileConfigs(Set<CompetenceEvidenceProfileConfig> evidenceProfileConfigs) {
        this.evidenceProfileConfigs = evidenceProfileConfigs;
    }

    @OneToMany(mappedBy = "competenceProfileConfig")
    public Set<CompetenceAssessmentProfileConfig> getCompetenceAssessmentProfileConfigs() {
        return competenceAssessmentProfileConfigs;
    }

    public void setCompetenceAssessmentProfileConfigs(Set<CompetenceAssessmentProfileConfig> competenceAssessmentProfileConfigs) {
        this.competenceAssessmentProfileConfigs = competenceAssessmentProfileConfigs;
    }
}
