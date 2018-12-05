package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.studentprofile.*;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.user.data.parameterobjects.CompetenceAssessmentWithGradeSummaryData;
import org.prosolo.services.user.data.parameterobjects.CompetenceProfileOptionsParam;
import org.prosolo.services.user.data.parameterobjects.CredentialAssessmentWithGradeSummaryData;
import org.prosolo.services.user.data.parameterobjects.CredentialProfileOptionsParam;
import org.prosolo.services.user.data.profile.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-22
 * @since 1.2.0
 */
@Component
public class CredentialProfileOptionsDataFactory extends ProfileDataFactory {

    public CredentialProfileOptionsData getCredentialProfileOptionsData(CredentialProfileOptionsParam param) {
        TargetCredential1 targetCredential = param.getTargetCredential();
        Optional<CredentialProfileConfig> credentialProfileConfig = param.getCredentialProfileConfig();
        return new CredentialProfileOptionsData(
                targetCredential.getId(),
                targetCredential.getCredential().getTitle(),
                getCompetenceProfileOptions(targetCredential.getCredential().getAssessmentConfig(), param.getCompetenceProfileOptionsParams()),
                getCredentialAssessmentsProfileOptionsData(
                        targetCredential.getCredential(),
                        param.getAssessments(),
                        credentialProfileConfig.isPresent() ? credentialProfileConfig.get().getCredentialAssessmentProfileConfigs() : Collections.emptySet()));
    }

    private List<AssessmentByTypeProfileOptionsData> getCredentialAssessmentsProfileOptionsData(Credential1 credential, Collection<CredentialAssessmentWithGradeSummaryData> assessments, Collection<CredentialAssessmentProfileConfig> assessmentProfileConfigs) {
        List<AssessmentByTypeProfileOptionsData> assessmentsByTypeProfileOptions = new ArrayList<>();
        Map<AssessmentType, List<CredentialAssessmentWithGradeSummaryData>> groupedAssessments =
                assessments
                        .stream()
                        .collect(Collectors.groupingBy(
                                ca-> ca.getCredentialAssessment().getType(),
                                LinkedHashMap::new,
                                Collectors.toList()));
        for (CredentialAssessmentConfig conf : credential.getAssessmentConfig()) {
            if (conf.isEnabled()) {
                List<SelectableData<AssessmentProfileData>> selectableAssessmentsForType = new ArrayList<>();
                List<CredentialAssessmentWithGradeSummaryData> assessmentsForType = groupedAssessments.get(conf.getAssessmentType());
                if (assessmentsForType != null) {
                    assessmentsForType.forEach(a -> selectableAssessmentsForType.add(
                            new SelectableData<>(
                                    getCredentialAssessmentProfileData(a.getCredentialAssessment(), a.getGradeSummary(), conf.getBlindAssessmentMode()), assessmentProfileConfigs.stream().filter(apConf -> apConf.getCredentialAssessment().getId() == a.getCredentialAssessment().getId()).findFirst().isPresent())));
                }
                assessmentsByTypeProfileOptions.add(new AssessmentByTypeProfileOptionsData(conf.getAssessmentType(), selectableAssessmentsForType));
            }
        }
        assessmentsByTypeProfileOptions.sort((a1, a2) -> compareAssessmentTypes(a1.getAssessmentType(), a2.getAssessmentType()));
        return assessmentsByTypeProfileOptions;
    }

    private List<CompetenceProfileOptionsData> getCompetenceProfileOptions(Collection<CredentialAssessmentConfig> credentialAssessmentConfigs, List<CompetenceProfileOptionsParam> params) {
        List<CompetenceProfileOptionsData> competences = new ArrayList<>();
        for (CompetenceProfileOptionsParam compParam : params) {
            TargetCompetence1 tc = compParam.getTargetCompetence();
            competences.add(getCompetenceProfileOptionsData(
                    tc,
                    credentialAssessmentConfigs,
                    compParam.getAssessments(),
                    compParam.getCompetenceProfileConfig()));
        }
        return competences;
    }

    private CompetenceProfileOptionsData getCompetenceProfileOptionsData(TargetCompetence1 tc, Collection<CredentialAssessmentConfig> credentialAssessmentConfigs, List<CompetenceAssessmentWithGradeSummaryData> assessments, Optional<CompetenceProfileConfig> profileConfig) {
        List<SelectableData<CompetenceEvidenceProfileData>> evidence = tc.getCompetence().getLearningPathType() == LearningPathType.EVIDENCE
                ? getCompetenceEvidenceProfileOptionsData(
                        tc.getEvidences(),
                        profileConfig.isPresent() ? profileConfig.get().getEvidenceProfileConfigs() : Collections.emptyList())
                : Collections.emptyList();
        List<AssessmentByTypeProfileOptionsData> assessmentsByType = getCompetenceAssessmentsProfileOptionsData(
                credentialAssessmentConfigs,
                assessments,
                profileConfig.isPresent() ? profileConfig.get().getCompetenceAssessmentProfileConfigs() : Collections.emptySet());

        return new CompetenceProfileOptionsData(
                tc.getId(),
                tc.getCompetence().getTitle(),
                evidence,
                assessmentsByType);
    }

    private List<SelectableData<CompetenceEvidenceProfileData>> getCompetenceEvidenceProfileOptionsData(Collection<CompetenceEvidence> evidences, Collection<CompetenceEvidenceProfileConfig> evidenceProfileConfigs) {
        List<SelectableData<CompetenceEvidenceProfileData>> evidence = new ArrayList<>();
        for (CompetenceEvidence ce : evidences) {
            evidence.add(new SelectableData<>(
                    getCompetenceEvidenceProfileData(ce),
                    evidenceProfileConfigs.stream().filter(conf -> conf.getCompetenceEvidence().getId() == ce.getId()).findFirst().isPresent()));
        }
        return evidence;
    }

    private List<AssessmentByTypeProfileOptionsData> getCompetenceAssessmentsProfileOptionsData(Collection<CredentialAssessmentConfig> credentialAssessmentConfigs, Collection<CompetenceAssessmentWithGradeSummaryData> assessments, Collection<CompetenceAssessmentProfileConfig> assessmentProfileConfigs) {
        List<AssessmentByTypeProfileOptionsData> assessmentsByTypeProfileOptions = new ArrayList<>();
        Map<AssessmentType, List<CompetenceAssessmentWithGradeSummaryData>> groupedAssessments =
                assessments
                        .stream()
                        .collect(Collectors.groupingBy(
                                ca-> ca.getCompetenceAssessment().getType(),
                                LinkedHashMap::new,
                                Collectors.toList()));
        /*
        TODO hack competences inherit blind assessment config from credentials and
        although it has its own enabled flag, it is dependent on credential and its 'enabled' flag
         */
        for (CredentialAssessmentConfig conf : credentialAssessmentConfigs) {
            if (conf.isEnabled()) {
                List<SelectableData<AssessmentProfileData>> selectableAssessmentsForType = new ArrayList<>();
                List<CompetenceAssessmentWithGradeSummaryData> assessmentsForType = groupedAssessments.get(conf.getAssessmentType());
                if (assessmentsForType != null) {
                    assessmentsForType.forEach(a -> selectableAssessmentsForType.add(
                            new SelectableData<>(
                                    getCompetenceAssessmentProfileData(a.getCompetenceAssessment(), a.getGradeSummary(), conf.getBlindAssessmentMode()), assessmentProfileConfigs.stream().filter(apConf -> apConf.getCompetenceAssessment().getId() == a.getCompetenceAssessment().getId()).findFirst().isPresent())));
                }
                assessmentsByTypeProfileOptions.add(new AssessmentByTypeProfileOptionsData(conf.getAssessmentType(), selectableAssessmentsForType));
            }
        }
        assessmentsByTypeProfileOptions.sort((a1, a2) -> compareAssessmentTypes(a1.getAssessmentType(), a2.getAssessmentType()));
        return assessmentsByTypeProfileOptions;
    }

}
