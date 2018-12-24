package org.prosolo.services.user.data.parameterobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.studentprofile.CompetenceProfileConfig;

import java.util.List;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-11-22
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CompetenceProfileOptionsParam {

    private final TargetCompetence1 targetCompetence;
    private final Optional<CompetenceProfileConfig> competenceProfileConfig;
    private final List<CompetenceAssessmentWithGradeSummaryData> assessments;
}
