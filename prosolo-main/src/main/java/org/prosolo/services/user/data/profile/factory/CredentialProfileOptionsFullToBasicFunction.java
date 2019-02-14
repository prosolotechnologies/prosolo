package org.prosolo.services.user.data.profile.factory;

import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.user.data.profile.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-29
 * @since 1.2.0
 */
@Component
public class CredentialProfileOptionsFullToBasicFunction implements Function<CredentialProfileOptionsData, CredentialProfileOptionsBasicData> {

    @Override
    public CredentialProfileOptionsBasicData apply(CredentialProfileOptionsData profileOptionsData) {
        return new CredentialProfileOptionsBasicData(
                profileOptionsData.getTargetCredentialId(),
                profileOptionsData.getCompetences()
                        .stream()
                        .map(compProfileOptionsData -> getCompetenceProfileOptionsBasicData(compProfileOptionsData))
                        .collect(Collectors.toList()),
                        getAssessmentsProfileOptionsBasicData(profileOptionsData.getAssessments()));
    }

    private CompetenceProfileOptionsBasicData getCompetenceProfileOptionsBasicData(CompetenceProfileOptionsData profileOptionsData) {
        return new CompetenceProfileOptionsBasicData(
                profileOptionsData.getTargetCompetenceId(),
                profileOptionsData.getEvidence().stream().map(ev -> new SelectableData<>(ev.getData().getCompetenceEvidenceId(), ev.isSelected())).collect(Collectors.toList()),
                getAssessmentsProfileOptionsBasicData(profileOptionsData.getAssessments()));
    }

    private List<SelectableData<Long>> getAssessmentsProfileOptionsBasicData(List<AssessmentByTypeProfileOptionsData> assesmentsByTypeProfileOptionsData) {
         return assesmentsByTypeProfileOptionsData
                 .stream()
                 .flatMap(assessmentsByType -> assessmentsByType.getAssessments().stream())
                 .map(assessment -> new SelectableData<>(assessment.getData().getAssessmentId(), assessment.isSelected()))
                 .collect(Collectors.toList());
    }
}
