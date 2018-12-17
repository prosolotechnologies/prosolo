package org.prosolo.common.domainmodel.credential;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2018-01-22
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential", "assessment_type"})})
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
