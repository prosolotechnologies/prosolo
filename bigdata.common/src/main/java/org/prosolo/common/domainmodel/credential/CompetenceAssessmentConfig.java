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
public class CompetenceAssessmentConfig extends AssessmentConfig {

    private Competence1 competence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public Competence1 getCompetence() {
        return competence;
    }

    public void setCompetence(Competence1 competence) {
        this.competence = competence;
    }


}
