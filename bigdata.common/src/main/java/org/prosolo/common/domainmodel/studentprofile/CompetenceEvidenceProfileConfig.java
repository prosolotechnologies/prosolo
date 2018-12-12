package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.credential.LearningEvidence;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Represents piece of competence evidence to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("4")
public class CompetenceEvidenceProfileConfig extends StudentProfileConfig {

    private static final long serialVersionUID = 8446745422185792044L;

    private CompetenceProfileConfig competenceProfileConfig;
    private LearningEvidence learningEvidence;

    @ManyToOne (fetch = FetchType.LAZY)
    public CompetenceProfileConfig getCompetenceProfileConfig() {
        return competenceProfileConfig;
    }

    public void setCompetenceProfileConfig(CompetenceProfileConfig competenceProfileConfig) {
        this.competenceProfileConfig = competenceProfileConfig;
    }

    @ManyToOne (fetch = FetchType.LAZY)
    public LearningEvidence getLearningEvidence() {
        return learningEvidence;
    }

    public void setLearningEvidence(LearningEvidence learningEvidence) {
        this.learningEvidence = learningEvidence;
    }
}
