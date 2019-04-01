package org.prosolo.services.nodes.data.organization;

import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.services.user.data.UserData;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bojan on 6/6/2017.
 */
public class OrganizationData implements Serializable {

    private long id;
    private OrganizationBasicData basicData;
    private OrganizationLearningStageData learningStageData;
    private OrganizationCategoryData categoryData;
    private OrganizationTokenData tokenData;

    public OrganizationData(){
        basicData = new OrganizationBasicData();
        learningStageData = new OrganizationLearningStageData();
        categoryData = new OrganizationCategoryData();
        tokenData = new OrganizationTokenData();
    }

    public OrganizationData(Organization organization){
        this();
        this.id = organization.getId();
        setTitle(organization.getTitle());
        setLearningInStagesEnabled(organization.isLearningInStagesEnabled());
        tokenData.setAssessmentTokensEnabled(organization.isAssessmentTokensEnabled());
        tokenData.setInitialNumberOfTokensGiven(organization.getInitialNumberOfTokensGiven());
        tokenData.setNumberOfEarnedTokensPerAssessment(organization.getNumberOfEarnedTokensPerAssessment());
        tokenData.setNumberOfSpentTokensPerRequest(organization.getNumberOfSpentTokensPerRequest());
    }

    public OrganizationData(Organization organization, List<UserData> chosenAdmins){
        this(organization);
        setAdmins(chosenAdmins);
    }

    public OrganizationData(long id, String title){
        this();
        this.id = id;
        setTitle(title);
    }

    public String getAdminsString() {
        String adminsString = "";
        List<UserData> admins = getAdmins();
        if (admins != null) {
            for (UserData a : admins) {
                if(!adminsString.isEmpty()) {
                    adminsString += ", ";
                }
                adminsString += a.getFullName();
            }
        }
        return adminsString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<LearningStageData> getLearningStages() {
        return learningStageData.getLearningStages();
    }

    public void addLearningStage(LearningStageData lStage) {
        learningStageData.addLearningStage(lStage);
    }

    public void addAllLearningStages(Collection<LearningStageData> learningStages) {
        learningStageData.addAllLearningStages(learningStages);
    }

    public List<LearningStageData> getLearningStagesForDeletion() {
        return learningStageData.getLearningStagesForDeletion();
    }

    public void addLearningStageForDeletion(LearningStageData learningStageForDeletion) {
        learningStageData.addLearningStageForDeletion(learningStageForDeletion);
    }

    public boolean isLearningInStagesEnabled() {
        return learningStageData.isLearningInStagesEnabled();
    }

    public void setLearningInStagesEnabled(boolean learningInStagesEnabled) {
        this.learningStageData.setLearningInStagesEnabled(learningInStagesEnabled);
    }

    public List<CredentialCategoryData> getCredentialCategories() {
        return categoryData.getCredentialCategories();
    }

    public void addCredentialCategory(CredentialCategoryData category) {
        categoryData.addCredentialCategory(category);
    }

    public void addAllCredentialCategories(Collection<CredentialCategoryData> categories) {
        categoryData.addAllCredentialCategories(categories);
    }

    public List<CredentialCategoryData> getCredentialCategoriesForDeletion() {
        return categoryData.getCredentialCategoriesForDeletion();
    }

    public void addCredentialCategoryForDeletion(CredentialCategoryData category) {
        categoryData.addCredentialCategoryForDeletion(category);
    }

    public OrganizationTokenData getTokenData() {
        return tokenData;
    }

    public OrganizationBasicData getBasicData() {
        return basicData;
    }

    public OrganizationLearningStageData getLearningStageData() {
        return learningStageData;
    }

    public OrganizationCategoryData getCategoryData() {
        return categoryData;
    }

}
