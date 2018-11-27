package org.prosolo.services.user.data.profile;

import org.prosolo.services.common.data.LazyInitData;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class CompetenceProfileData implements Serializable {

    private static final long serialVersionUID = 4566644824168327473L;

    private final long id;
    private final String title;
    private final LazyInitData<CompetenceEvidenceProfileData> evidence;
    private final LazyInitData<AssessmentByTypeProfileData> assessments;

    public CompetenceProfileData(long id, String title, LazyInitData<CompetenceEvidenceProfileData> evidence, LazyInitData<AssessmentByTypeProfileData> assessments) {
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

    public LazyInitData<CompetenceEvidenceProfileData> getEvidence() {
        return evidence;
    }

    public LazyInitData<AssessmentByTypeProfileData> getAssessments() {
        return assessments;
    }
}
