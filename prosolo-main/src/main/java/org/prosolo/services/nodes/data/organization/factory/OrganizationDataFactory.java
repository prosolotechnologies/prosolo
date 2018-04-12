package org.prosolo.services.nodes.data.organization.factory;

import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.data.organization.OrganizationData;
import org.prosolo.services.nodes.data.UserData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        organizationData.addAllLearningStages(learningStages);
        organizationData.addAllCredentialCategories(credentialCategories);

        return organizationData;
    }

}
