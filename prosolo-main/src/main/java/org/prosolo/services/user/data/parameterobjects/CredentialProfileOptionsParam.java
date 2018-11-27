package org.prosolo.services.user.data.parameterobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.studentprofile.CredentialAssessmentProfileConfig;
import org.prosolo.common.domainmodel.studentprofile.CredentialProfileConfig;

import java.util.List;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-11-22
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CredentialProfileOptionsParam {

    private final TargetCredential1 targetCredential;
    private final List<CredentialAssessmentWithGradeSummaryData> assessments;
    private final Optional<CredentialProfileConfig> credentialProfileConfig;
    private final List<CompetenceProfileOptionsParam> competenceProfileOptionsParams;
}
