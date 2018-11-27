package org.prosolo.services.user.data.profile;

import org.prosolo.services.common.data.SelectableData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-20
 * @since 1.2.0
 */
public class CompetenceProfileOptionsData implements Serializable {

    private static final long serialVersionUID = -6284496293208098696L;

    private final long id;
    private final String title;
    private final List<SelectableData<CompetenceEvidenceProfileData>> evidence;
    private final List<AssessmentByTypeProfileOptionsData> assessments;

    public CompetenceProfileOptionsData(long id, String title, List<SelectableData<CompetenceEvidenceProfileData>> evidence, List<AssessmentByTypeProfileOptionsData> assessments) {
        this.id = id;
        this.title = title;
        this.evidence = evidence;
        this.assessments = assessments;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<SelectableData<CompetenceEvidenceProfileData>> getEvidence() {
        return evidence;
    }

    public List<AssessmentByTypeProfileOptionsData> getAssessments() {
        return assessments;
    }
}
