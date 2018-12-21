package org.prosolo.services.user.data.parameterobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;

/**
 * @author stefanvuckovic
 * @date 2018-11-22
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CredentialAssessmentWithGradeSummaryData {

    private final CredentialAssessment credentialAssessment;
    private final AssessmentGradeSummary gradeSummary;

}
