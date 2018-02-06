package org.prosolo.services.nodes.data.assessments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2017-10-26
 * @since 1.1.0
 */
public class CredentialAssessmentsSummaryData implements Serializable {

    private static final long serialVersionUID = 2506027909576746426L;

    private long id;
    private String title;
    private boolean mandatoryOrder;
    private List<CompetenceAssessmentsSummaryData> competencesAssessmentSummary;

    public CredentialAssessmentsSummaryData() {
        this.competencesAssessmentSummary = new ArrayList<>();
    }

    public void addCompetenceSummary(CompetenceAssessmentsSummaryData compSummary) {
        competencesAssessmentSummary.add(compSummary);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMandatoryOrder() {
        return mandatoryOrder;
    }

    public void setMandatoryOrder(boolean mandatoryOrder) {
        this.mandatoryOrder = mandatoryOrder;
    }

    public List<CompetenceAssessmentsSummaryData> getCompetencesAssessmentSummary() {
        return competencesAssessmentSummary;
    }

    public void setCompetencesAssessmentSummary(List<CompetenceAssessmentsSummaryData> competencesAssessmentSummary) {
        this.competencesAssessmentSummary = competencesAssessmentSummary;
    }
}
