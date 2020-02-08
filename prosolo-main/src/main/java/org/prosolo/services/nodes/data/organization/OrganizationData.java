package org.prosolo.services.nodes.data.organization;

import lombok.Getter;
import lombok.Setter;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.services.user.data.UserData;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class holding the organization data.
 *
 * @author Bojan Trifkovic
 * @date 2017-06-06
 * @since 1.0
 */
@Getter @Setter
public class OrganizationData implements Serializable {

    private long id;
    private OrganizationBasicData basicData;
    private EvidenceRepositoryPluginData evidenceRepositoryPluginData;
    private LearningStagesPluginData learningStagesPluginData;
    private CredentialCategoriesPluginData credentialCategoriesPluginData;
    private AssessmentTokensPluginData assessmentTokensPluginData;

    public OrganizationData() {
        basicData = new OrganizationBasicData();
    }

    public OrganizationData(Organization organization) {
        this();
        this.id = organization.getId();
        setTitle(organization.getTitle());
    }

    public OrganizationData(Organization organization, List<UserData> chosenAdmins) {
        this(organization);
        setAdmins(chosenAdmins);
    }

    public OrganizationData(long id, String title) {
        this();
        this.id = id;
        setTitle(title);
    }

    public String getAdminsString() {
        return getAdmins().stream()
                .map(a -> a.getFullName())
                .collect(Collectors.joining(", "));
    }

    public String getTitle() {
        return basicData.getTitle();
    }

    public void setTitle(String title) {
        basicData.setTitle(title);
    }

    public List<UserData> getAdmins() {
        return basicData.getAdmins();
    }

    public void setAdmins(List<UserData> admins) {
        basicData.setAdmins(admins);
    }

    public void addLearningStage(LearningStageData lStage) {
        learningStagesPluginData.addLearningStage(lStage);
    }

    public List<LearningStageData> getLearningStagesForDeletion() {
        return learningStagesPluginData.getLearningStagesForDeletion();
    }

    public void addLearningStageForDeletion(LearningStageData learningStageForDeletion) {
        learningStagesPluginData.addLearningStageForDeletion(learningStageForDeletion);
    }

    public void setLearningInStagesEnabled(boolean learningInStagesEnabled) {
        this.learningStagesPluginData.setEnabled(learningInStagesEnabled);
    }

    public List<CredentialCategoryData> getCredentialCategories() {
        return credentialCategoriesPluginData.getCredentialCategories();
    }

    public void addCredentialCategory(CredentialCategoryData category) {
        credentialCategoriesPluginData.addCredentialCategory(category);
    }

    public List<CredentialCategoryData> getCredentialCategoriesForDeletion() {
        return credentialCategoriesPluginData.getCredentialCategoriesForDeletion();
    }

    public void addCredentialCategoryForDeletion(CredentialCategoryData category) {
        credentialCategoriesPluginData.addCredentialCategoryForDeletion(category);
    }

}
