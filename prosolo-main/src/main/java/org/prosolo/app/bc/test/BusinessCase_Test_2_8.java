package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
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
        enrollToDelivery(events, organization, credential6Delivery1, userGeorgeYoung);
        ///////////////////////////
        // enroll users to competencies and complete them
        ///////////////////////////
        List<CompetenceData1> credential2Delivery1Competences = ServiceLocator.getInstance().getService(Competence1Manager.class)
                .getCompetencesForCredential(credential2Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential2Delivery1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1Competences.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        LearningEvidenceData ev1 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential2Delivery1Comp1Target.getId(),
                userGeorgeYoung);
        markCompetencyAsCompleted(events, credential2Delivery1Comp1Target.getId(), userGeorgeYoung);

        List<CompetenceData1> credential6Delivery1Competences = ServiceLocator.getInstance().getService(Competence1Manager.class)
                .getCompetencesForCredential(credential6Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential6Delivery1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1Competences.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp1Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp1Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1Competences.get(1).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp2Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1Competences.get(2).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp3Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1Competences.get(3).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp4Target.getId(), userGeorgeYoung);
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
