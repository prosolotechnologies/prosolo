package org.prosolo.common.domainmodel.credential;

import org.prosolo.common.domainmodel.general.BaseEntity;

import javax.persistence.*;

/**
 * @author stefanvuckovic
 * @date 2017-12-04
 * @since 1.2.0
 */
@Entity
public class CompetenceEvidence extends BaseEntity {

    private TargetCompetence1 competence;
    private LearningEvidence evidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public TargetCompetence1 getCompetence() {
        return competence;
    }

    public void setCompetence(TargetCompetence1 competence) {
        this.competence = competence;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    public LearningEvidence getEvidence() {
        return evidence;
    }

    public void setEvidence(LearningEvidence evidence) {
        this.evidence = evidence;
    }
}
