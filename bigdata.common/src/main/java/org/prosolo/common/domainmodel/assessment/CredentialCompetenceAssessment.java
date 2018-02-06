package org.prosolo.common.domainmodel.assessment;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-12-26
 * @since 1.2.0
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"credential_assessment", "competence_assessment"})})
public class CredentialCompetenceAssessment extends BaseEntity {

    private CredentialAssessment credentialAssessment;
    private CompetenceAssessment competenceAssessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public CredentialAssessment getCredentialAssessment() {
        return credentialAssessment;
    }

    public void setCredentialAssessment(CredentialAssessment credentialAssessment) {
        this.credentialAssessment = credentialAssessment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public CompetenceAssessment getCompetenceAssessment() {
        return competenceAssessment;
    }

    public void setCompetenceAssessment(CompetenceAssessment competenceAssessment) {
        this.competenceAssessment = competenceAssessment;
    }
}
