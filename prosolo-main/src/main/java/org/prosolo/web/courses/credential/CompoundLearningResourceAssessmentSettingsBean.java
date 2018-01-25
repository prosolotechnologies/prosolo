package org.prosolo.web.courses.credential;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.nodes.data.AssessmentTypeConfig;
import org.prosolo.web.courses.LearningResourceAssessmentSettingsBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-01-24
 * @since 1.2.0
 */
public abstract class CompoundLearningResourceAssessmentSettingsBean extends LearningResourceAssessmentSettingsBean {


    public List<AssessmentTypeConfig> getAssessmentTypes() {
        List<AssessmentTypeConfig> types = new ArrayList<>();
        for (AssessmentType at : AssessmentType.values()) {
            //instructor assessment is mandatory and should be enabled
            boolean instructorAssessment = at == AssessmentType.INSTRUCTOR_ASSESSMENT;
            types.add(new AssessmentTypeConfig(0, at, instructorAssessment, instructorAssessment));
        }
        return types;
    }
}
