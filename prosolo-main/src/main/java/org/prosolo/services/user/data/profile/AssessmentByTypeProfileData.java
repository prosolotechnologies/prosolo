package org.prosolo.services.user.data.profile;

import org.prosolo.common.domainmodel.assessment.AssessmentType;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-15
 * @since 1.2.0
 */
public class AssessmentByTypeProfileData implements Serializable {

    private static final long serialVersionUID = -3608777079207808629L;

    private final AssessmentType assessmentType;
    private final List<AssessmentProfileData> assessments;

    public AssessmentByTypeProfileData(AssessmentType assessmentType, List<AssessmentProfileData> assessments) {
        this.assessmentType = assessmentType;
        this.assessments = assessments;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public List<AssessmentProfileData> getAssessments() {
        return assessments;
    }
}
