package org.prosolo.services.nodes.data.organization.factory;

import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.settings.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.organization.*;
import org.prosolo.services.user.data.UserData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-01
 * @since 1.0.0
 */
@Component
public class OrganizationDataFactory {

    public OrganizationData getOrganizationData(Organization organization, List<User> users, List<LearningStageData> learningStages, List<CredentialCategoryData> credentialCategories) throws NullPointerException {
        List<UserData> userDataList = new ArrayList<>();
        if (users == null) {
            throw new NullPointerException("Users cannot be null");
        }
        for (User u : users) {
            UserData ud = new UserData(u);
            userDataList.add(ud);
        }
        OrganizationData organizationData = new OrganizationData(organization, userDataList);

        // learning stages plugin
        LearningStagesPlugin learningStagesPlugin = (LearningStagesPlugin) organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.LEARNING_STAGES).findAny().get();
        LearningStagesPluginData learningStagesPluginData = new LearningStagesPluginData(learningStagesPlugin);
        learningStagesPluginData.setLearningStages(learningStages);
        organizationData.setLearningStagesPluginData(learningStagesPluginData);

        // evidence repository plugin
        EvidenceRepositoryPlugin evidenceRepositoryPlugin = (EvidenceRepositoryPlugin) organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.EVIDENCE_REPOSITORY).findAny().get();
        organizationData.setEvidenceRepositoryPluginData(new EvidenceRepositoryPluginData(evidenceRepositoryPlugin));

        // credential categories plugin
        CredentialCategoriesPlugin credentialCategoriesPlugin = (CredentialCategoriesPlugin) organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.CREDENTIAL_CATEGORIES).findAny().get();
        CredentialCategoriesPluginData credentialCategoriesPluginData = new CredentialCategoriesPluginData(credentialCategoriesPlugin);
        credentialCategoriesPluginData.setCredentialCategories(credentialCategories);
        organizationData.setCredentialCategoriesPluginData(credentialCategoriesPluginData);

        // assessment tokens plugin
        AssessmentTokensPlugin assessmentTokensPlugin = (AssessmentTokensPlugin) organization.getPlugins().stream().filter(p -> p.getType() == OrganizationPluginType.ASSESSMENT_TOKENS).findAny().get();
        organizationData.setAssessmentTokensPluginData(new AssessmentTokensPluginData(assessmentTokensPlugin));

        return organizationData;
    }

}
