package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-01-22
 * @since 1.3
 */
public class BusinessCase_Test_2_8 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_8.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        ///////////////////////////
        // create credential category and set it for credentials
        ///////////////////////////
        createCredentialCategories(events, "Category 1");
        CredentialCategoryData category = ServiceLocator.getInstance().getService(OrganizationManager.class)
                .getOrganizationCredentialCategoriesData(organization.getId()).get(0);
        assignCategoryToCredential(events, credential2.getId(), category, userNickPowell);
        assignCategoryToCredential(events, credential3.getId(), category, userNickPowell);

        ///////////////////////////
        // give privilege to users
        ///////////////////////////
        givePrivilegeToUsersOnDelivery(events, credential1Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGeorgeYoung));
        givePrivilegeToUsersOnDelivery(events, credential2Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGeorgeYoung));
        givePrivilegeToUsersOnDelivery(events, credential3Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGeorgeYoung));
        givePrivilegeToUsersOnDelivery(events, credential4Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGeorgeYoung));
        givePrivilegeToUsersOnDelivery(events, credential6Delivery1, UserGroupPrivilege.Learn, userNickPowell, organization, List.of(userGeorgeYoung));
        ///////////////////////////
        // enroll users to deliveries
        ///////////////////////////
        enrollToDelivery(events, organization, credential1Delivery1, userGeorgeYoung);
        enrollToDelivery(events, organization, credential2Delivery1, userGeorgeYoung);
        ///////////////////////////
        // bookmark credentials
        ///////////////////////////
        bookmarkCredential(events, credential3Delivery1.getId(), userGeorgeYoung);
        bookmarkCredential(events, credential4Delivery1.getId(), userGeorgeYoung);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.8";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
