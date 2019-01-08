package org.prosolo.services.user.data.profile.factory;

import org.prosolo.common.domainmodel.credential.CredentialCategory;
import org.prosolo.common.domainmodel.studentprofile.*;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.grading.AssessmentGradeSummary;
import org.prosolo.services.common.data.LazyInitCollection;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.user.data.profile.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author stefanvuckovic
 * @date 2018-11-19
 * @since 1.2.0
 */
@Component
public class CredentialProfileDataFactory extends ProfileDataFactory {

    @Inject private GradeDataFactory gradeDataFactory;

    public CredentialProfileData getCredentialProfileData(CredentialProfileConfig credentialProfileConfig, long assessmentsCount, long competencesCount) {
        CredentialCategory category = credentialProfileConfig.getTargetCredential().getCredential().getCategory();
        CredentialCategoryData categoryData = category != null ? new CredentialCategoryData(category.getId(), category.getTitle(), false) : null;
        return new CredentialProfileData(
                credentialProfileConfig.getId(),
                credentialProfileConfig.getTargetCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getId(),
                credentialProfileConfig.getTargetCredential().getCredential().getTitle(),
                credentialProfileConfig.getTargetCredential().getCredential().getDescription(),
                credentialProfileConfig.getTargetCredential().getCredential().getTags().stream().map(tag -> tag.getTitle()).collect(Collectors.toList()),
                DateUtil.getMillisFromDate(credentialProfileConfig.getTargetCredential().getDateFinished()),
                new LazyInitCollection<>(assessmentsCount),
                new LazyInitCollection<>(competencesCount),
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
                new LazyInitCollection<>(evidenceCount),
                new LazyInitCollection<>(assessmentsCount));
    }

    public CompetenceEvidenceProfileData getCompetenceEvidenceProfileData(CompetenceEvidenceProfileConfig competenceEvidenceProfileConfig) {
        return getCompetenceEvidenceProfileData(competenceEvidenceProfileConfig.getCompetenceEvidence());
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
                                            gradeDataFactory.getGradeDataFromGrade(conf.getGrade())),
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
                                            gradeDataFactory.getGradeDataFromGrade(conf.getGrade())),
                                Collectors.toList())))
                .entrySet().stream()
                .map(entry -> new AssessmentByTypeProfileData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        assessments.sort((a1, a2) -> compareAssessmentTypes(a1.getAssessmentType(), a2.getAssessmentType()));
        return assessments;
    }

}
