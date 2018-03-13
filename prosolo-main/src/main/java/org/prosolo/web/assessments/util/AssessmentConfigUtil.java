package org.prosolo.web.assessments.util;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2018-03-12
 * @since 1.2.0
 */
public class AssessmentConfigUtil {

    public static boolean isPeerAssessmentEnabled(List<AssessmentTypeConfig> assessmentTypesConfig) {
        return isAssessmentTypeEnabled(assessmentTypesConfig, AssessmentType.PEER_ASSESSMENT);
    }

    public static boolean isSelfAssessmentEnabled(List<AssessmentTypeConfig> assessmentTypesConfig) {
        return isAssessmentTypeEnabled(assessmentTypesConfig, AssessmentType.SELF_ASSESSMENT);
    }

    private static boolean isAssessmentTypeEnabled(List<AssessmentTypeConfig> assessmentTypesConfig, AssessmentType type) {
        if (assessmentTypesConfig == null || assessmentTypesConfig.isEmpty()) {
            return false;
        }
        AssessmentTypeConfig aType = assessmentTypesConfig.stream().filter(t -> t.getType() == type).findFirst().get();
        return aType.isEnabled();
    }
}
