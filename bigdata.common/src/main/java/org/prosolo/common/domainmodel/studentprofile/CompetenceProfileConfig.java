package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.credential.TargetCompetence1;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

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
}
