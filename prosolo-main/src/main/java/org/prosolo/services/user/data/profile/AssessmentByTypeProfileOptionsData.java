package org.prosolo.services.user.data.profile;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.common.data.SelectableData;

import java.io.Serializable;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-11-21
 * @since 1.2.0
 */
public class AssessmentByTypeProfileOptionsData implements Serializable {

    private static final long serialVersionUID = -4263973826447426058L;

    private final AssessmentType assessmentType;
    private final List<SelectableData<AssessmentProfileData>> assessments;

    public AssessmentByTypeProfileOptionsData(AssessmentType assessmentType, List<SelectableData<AssessmentProfileData>> assessments) {
        this.assessmentType = assessmentType;
        this.assessments = assessments;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public List<SelectableData<AssessmentProfileData>> getAssessments() {
        return assessments;
    }
}
