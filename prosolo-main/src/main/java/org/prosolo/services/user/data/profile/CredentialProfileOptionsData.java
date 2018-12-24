package org.prosolo.services.user.data.profile;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-20
 * @since 1.2.0
 */
public class CredentialProfileOptionsData implements Serializable {

    private static final long serialVersionUID = 7664743496622047694L;

    private final long targetCredentialId;
    private final String title;
    private final List<CompetenceProfileOptionsData> competences;
    private final List<AssessmentByTypeProfileOptionsData> assessments;

    public CredentialProfileOptionsData(long targetCredentialId, String title, List<CompetenceProfileOptionsData> competences, List<AssessmentByTypeProfileOptionsData> assessments) {
        this.targetCredentialId = targetCredentialId;
        this.title = title;
        this.competences = competences;
        this.assessments = assessments;
    }

    public long getTargetCredentialId() {
        return targetCredentialId;
    }

    public String getTitle() {
        return title;
    }

    public List<CompetenceProfileOptionsData> getCompetences() {
        return competences;
    }

    public List<AssessmentByTypeProfileOptionsData> getAssessments() {
        return assessments;
    }
    
}
