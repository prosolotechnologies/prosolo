package org.prosolo.services.assessment.data;

import org.prosolo.services.nodes.data.credential.CredentialIdData;

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

    private CredentialIdData credentialIdData;
    private boolean mandatoryOrder;
    private List<CompetenceAssessmentsSummaryData> competencesAssessmentSummary;

    public CredentialAssessmentsSummaryData() {
        this.competencesAssessmentSummary = new ArrayList<>();
        this.credentialIdData = new CredentialIdData(false);
    }

    public void addCompetenceSummary(CompetenceAssessmentsSummaryData compSummary) {
        competencesAssessmentSummary.add(compSummary);
    }

    public long getId() {
        return credentialIdData.getId();
    }

    public String getTitle() {
        return credentialIdData.getTitle();
    }

    public CredentialIdData getCredentialIdData() {
        return credentialIdData;
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
