package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialAssessmentConfig;
import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.studentprofile.*;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.common.data.LazyInitData;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.services.user.data.parameterobjects.CredentialAssessmentWithGradeSummaryData;
import org.prosolo.services.user.data.profile.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-19
 * @since 1.2.0
 */
@Component
public class CredentialProfileDataFactory extends ProfileDataFactory {

    public CredentialProfileData getCredentialProfileData(CredentialProfileConfig credentialProfileConfig, long assessmentsCount, long competencesCount) {
        CredentialCategory category = credentialProfileConfig.getTargetCredential().getCredential().getCategory();
        CredentialCategoryData categoryData = category != null ? new CredentialCategoryData(category.getId(), category.getTitle(), false) : null;
        return new CredentialProfileData(
                credentialProfileConfig.getId(),
                credentialProfileConfig.getTargetCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getTitle(),
                credentialProfileConfig.getTargetCredential().getCredential().getDescription(),
                TimeUtil.getHoursAndMinutesInString(credentialProfileConfig.getTargetCredential().getCredential().getDuration()),
                credentialProfileConfig.getTargetCredential().getCredential().getTags().stream().map(tag -> tag.getTitle()).collect(Collectors.toList()),
                DateUtil.getMillisFromDate(credentialProfileConfig.getTargetCredential().getDateFinished()),
                new LazyInitData<>(assessmentsCount),
                new LazyInitData<>(competencesCount),
                categoryData);
    }

    /**
     * This method assumes that credentials are already sorted by category
     *
     * @param credentialsSortedByCategory
     * @return
     */
    public List<CategorizedCredentialsProfileData> groupCredentialsByCategory(List<CredentialProfileData> credentialsSortedByCategory) {
        if (credentialsSortedByCategory == null) {
            return null;
        }
        if (credentialsSortedByCategory.isEmpty()) {
            return new ArrayList<>();
        }
        List<CategorizedCredentialsProfileData> categorizedCredentials = new ArrayList<>();
        CredentialCategoryData currentCategory = null;
        List<CredentialProfileData> credentialsInCurrentCategory = null;
        boolean first = true;
        for (CredentialProfileData cd : credentialsSortedByCategory) {
            if (!(cd.getCategory() == currentCategory || (cd.getCategory() != null && currentCategory != null && cd.getCategory().getId() == currentCategory.getId())) || first) {
                //if category is different than current one, we should add current data to the list because data for current category is collected
                if (!first) {
                    categorizedCredentials.add(new CategorizedCredentialsProfileData(currentCategory, credentialsInCurrentCategory));
                } else {
                    first = false;
                }
                currentCategory = cd.getCategory();
                credentialsInCurrentCategory = new ArrayList<>();
            }
            credentialsInCurrentCategory.add(cd);
        }
        //add last category with credentials
        categorizedCredentials.add(new CategorizedCredentialsProfileData(currentCategory, credentialsInCurrentCategory));
        return categorizedCredentials;
    }

    public CompetenceProfileData getCompetenceProfileData(CompetenceProfileConfig compProfileConfig, long evidenceCount, long assessmentsCount) {
        return new CompetenceProfileData(
                compProfileConfig.getId(),
                compProfileConfig.getTargetCompetence().getCompetence().getTitle(),
                new LazyInitData<>(evidenceCount),
                new LazyInitData<>(assessmentsCount));
    }

    public CompetenceEvidenceProfileData getCompetenceEvidenceProfileData(CompetenceEvidenceProfileConfig competenceEvidenceProfileConfig) {
        return new CompetenceEvidenceProfileData(
                competenceEvidenceProfileConfig.getCompetenceEvidence().getEvidence().getId(),
                competenceEvidenceProfileConfig.getCompetenceEvidence().getId(),
                competenceEvidenceProfileConfig.getCompetenceEvidence().getEvidence().getTitle(),
                competenceEvidenceProfileConfig.getCompetenceEvidence().getEvidence().getType(),
                competenceEvidenceProfileConfig.getCompetenceEvidence().getEvidence().getUrl(),
                DateUtil.getMillisFromDate(competenceEvidenceProfileConfig.getCompetenceEvidence().getDateCreated()));
    }

    public List<AssessmentByTypeProfileData> getCredentialAssessmentsProfileData(List<CredentialAssessmentProfileConfig> assessmentProfileConfigs) {
        List<AssessmentByTypeProfileData> assessments = assessmentProfileConfigs
                .stream()
                .collect(Collectors.groupingBy(
                        conf -> conf.getCredentialAssessment().getType(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                conf -> getCredentialAssessmentProfileData(
                                        conf.getCredentialAssessment(),
                                        new AssessmentGradeSummary(conf.getGrade(), conf.getMaxGrade()),
                                        getBlindAssessmentMode(conf, conf.getCredentialAssessment().getType())),
                                Collectors.toList())))
                .entrySet().stream()
                .map(entry -> new AssessmentByTypeProfileData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        assessments.sort((a1, a2) -> compareAssessmentTypes(a1.getAssessmentType(), a2.getAssessmentType()));
        return assessments;
    }

    public List<AssessmentByTypeProfileData> getCompetenceAssessmentsProfileData(List<CompetenceAssessmentProfileConfig> assessmentProfileConfigs) {
        List<AssessmentByTypeProfileData> assessments = assessmentProfileConfigs
                .stream()
                .collect(Collectors.groupingBy(
                        conf -> conf.getCompetenceAssessment().getType(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                conf -> getCompetenceAssessmentProfileData(
                                        conf.getCompetenceAssessment(),
                                        new AssessmentGradeSummary(conf.getGrade(), conf.getMaxGrade()),
                                        /*
                                        we use credential assessment config here because it overrides competency assessment config
                                        when there is a credential competency is added to, which is the case here
                                        where we observe this competency as a part of the credential on profile
                                         */
                                        getBlindAssessmentMode(conf, conf.getCompetenceAssessment().getType())),
                                Collectors.toList())))
                .entrySet().stream()
                .map(entry -> new AssessmentByTypeProfileData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        assessments.sort((a1, a2) -> compareAssessmentTypes(a1.getAssessmentType(), a2.getAssessmentType()));
        return assessments;
    }

    private BlindAssessmentMode getBlindAssessmentMode(AssessmentProfileConfig conf, AssessmentType assessmentType) {
        return conf.getTargetCredential().getCredential().getAssessmentConfig()
                .stream()
                .filter(credAssessmentConf -> credAssessmentConf.getAssessmentType() == assessmentType)
                .findFirst()
                .get()
                .getBlindAssessmentMode();
    }

}
