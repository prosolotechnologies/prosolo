package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.credential.TargetCredential1;

import javax.persistence.*;
import java.util.Set;

/**
 * Represents credential to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("1")
public class CredentialProfileConfig extends StudentProfileConfig {

    private static final long serialVersionUID = 8195021345667577462L;

    /*
    TODO hack this is a way to ensure constraint that there can only be
    one CredentialProfileConfig per target credential. This class inherits target credential
    from StudentProfileConfig but unique constraint can't be defined on that column since it
    is used by other config classes for which this column is not unique.
    This hack is needed because Mysql does not support filtered unique index which would allow
    us to define unique constraint only if specific condition is met.
     */
    private TargetCredential1 credentialProfileConfigTargetCredential;
    private Set<CompetenceProfileConfig> competenceProfileConfigs;
    private Set<CredentialAssessmentProfileConfig> credentialAssessmentProfileConfigs;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    public TargetCredential1 getCredentialProfileConfigTargetCredential() {
        return credentialProfileConfigTargetCredential;
    }

    public void setCredentialProfileConfigTargetCredential(TargetCredential1 credentialProfileConfigTargetCredential) {
        this.credentialProfileConfigTargetCredential = credentialProfileConfigTargetCredential;
    }

    @OneToMany(mappedBy = "credentialProfileConfig")
    public Set<CompetenceProfileConfig> getCompetenceProfileConfigs() {
        return competenceProfileConfigs;
    }

    public void setCompetenceProfileConfigs(Set<CompetenceProfileConfig> competenceProfileConfigs) {
        this.competenceProfileConfigs = competenceProfileConfigs;
    }

    @OneToMany(mappedBy = "credentialProfileConfig", targetEntity = CredentialAssessmentProfileConfig.class)
    public Set<CredentialAssessmentProfileConfig> getCredentialAssessmentProfileConfigs() {
        return credentialAssessmentProfileConfigs;
    }

    public void setCredentialAssessmentProfileConfigs(Set<CredentialAssessmentProfileConfig> credentialAssessmentProfileConfigs) {
        this.credentialAssessmentProfileConfigs = credentialAssessmentProfileConfigs;
    }
}
