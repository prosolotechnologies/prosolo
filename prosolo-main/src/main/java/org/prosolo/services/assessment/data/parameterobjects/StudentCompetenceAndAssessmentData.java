package org.prosolo.services.assessment.data.parameterobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.services.nodes.data.competence.CompetenceData1;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2019-02-11
 * @since 1.3
 */
@Getter
@AllArgsConstructor
public class StudentCompetenceAndAssessmentData implements Serializable {

    private final CompetenceData1 competenceData;
    private final CompetenceAssessment competenceAssessment;
}
