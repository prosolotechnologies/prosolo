package org.prosolo.common.domainmodel.studentprofile;

import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * Represents competence assessment to be displayed on student's profile
 *
 * @author stefanvuckovic
 * @date 2018-10-11
 * @since 1.2.0
 */
@Entity
@DiscriminatorValue("5")
public class CompetenceAssessmentProfileConfig extends AssessmentProfileConfig {

    private static final long serialVersionUID = 5510029209226370144L;

    private CompetenceProfileConfig competenceProfileConfig;
    private CompetenceAssessment competenceAssessment;

    @ManyToOne (fetch = FetchType.LAZY)
    public CompetenceProfileConfig getCompetenceProfileConfig() {
        return competenceProfileConfig;
    }

    public void setCompetenceProfileConfig(CompetenceProfileConfig competenceProfileConfig) {
        this.competenceProfileConfig = competenceProfileConfig;
    }

    @ManyToOne (fetch = FetchType.LAZY)
    public CompetenceAssessment getCompetenceAssessment() {
        return competenceAssessment;
    }

    public void setCompetenceAssessment(CompetenceAssessment competenceAssessment) {
        this.competenceAssessment = competenceAssessment;
    }
}
