package org.prosolo.services.user.data.parameterobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.services.user.data.profile.grade.GradeData;

/**
 * @author stefanvuckovic
 * @date 2018-11-22
 * @since 1.2.0
 */
@AllArgsConstructor
@Getter
public class CompetenceAssessmentWithGradeSummaryData {

    private final CompetenceAssessment competenceAssessment;
    private final GradeData gradeSummary;

}
