package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.assessment.CredentialAssessment;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Represents credential assessment to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("3")
public class CredentialAssessmentProfileConfig extends AssessmentProfileConfig {

    private static final long serialVersionUID = 2215726183826979417L;

    private CredentialProfileConfig credentialProfileConfig;
    private CredentialAssessment credentialAssessment;

    @ManyToOne (fetch = FetchType.LAZY)
    public CredentialProfileConfig getCredentialProfileConfig() {
        return credentialProfileConfig;
    }

    public void setCredentialProfileConfig(CredentialProfileConfig credentialProfileConfig) {
        this.credentialProfileConfig = credentialProfileConfig;
    }

    @ManyToOne (fetch = FetchType.LAZY)
    public CredentialAssessment getCredentialAssessment() {
        return credentialAssessment;
    }

    public void setCredentialAssessment(CredentialAssessment credentialAssessment) {
        this.credentialAssessment = credentialAssessment;
    }
}
