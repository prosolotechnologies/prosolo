package org.prosolo.services.assessment.data;

/**
 * @author stefanvuckovic
 * @date 2019-04-08
 * @since 1.3
 */
public class CompetenceAssessmentData extends AssessmentData {

    private long competenceId;
    private String competenceTitle;

    public long getCompetenceId() {
        return competenceId;
    }

    public void setCompetenceId(long competenceId) {
        this.competenceId = competenceId;
    }

    public String getCompetenceTitle() {
        return competenceTitle;
    }

    public void setCompetenceTitle(String competenceTitle) {
        this.competenceTitle = competenceTitle;
    }
}
