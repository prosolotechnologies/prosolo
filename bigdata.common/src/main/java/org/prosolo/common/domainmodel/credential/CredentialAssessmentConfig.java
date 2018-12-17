package org.prosolo.common.domainmodel.credential;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author stefanvuckovic
 * @date 2018-01-22
 * @since 1.2.0
 */
@Entity
//unique constraint added from the script
public class CredentialAssessmentConfig extends AssessmentConfig {

    private Credential1 credential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Credential1 getCredential() {
        return credential;
    }

    public void setCredential(Credential1 credential) {
        this.credential = credential;
    }

}
