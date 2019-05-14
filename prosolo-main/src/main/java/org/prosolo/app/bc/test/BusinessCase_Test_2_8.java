package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.EventQueue;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.ActivityType;
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
        enrollToDelivery(events, credential1Delivery1, userGeorgeYoung);
        enrollToDelivery(events, credential2Delivery1, userGeorgeYoung);
        enrollToDelivery(events, credential6Delivery1, userGeorgeYoung);
        ///////////////////////////
        // enroll users to competencies and complete them
        ///////////////////////////
        List<CompetenceData1> credential2Delivery1Competences = ServiceLocator.getInstance().getService(Competence1Manager.class)
                .getCompetencesForCredential(credential2Delivery1.getId(), userGeorgeYoung.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential2Delivery1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
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
        TargetCompetence1 credential6Delivery1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Delivery1Competences.get(0).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp1Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp1Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Delivery1Competences.get(1).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp2Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Delivery1Competences.get(2).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp3Target.getId(), userGeorgeYoung);
        TargetCompetence1 credential6Delivery1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential6Delivery1.getId(), credential6Delivery1Competences.get(3).getCompetenceId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
        attachExistingEvidenceToCompetence(ev1.getId(), credential6Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        markCompetencyAsCompleted(events, credential6Delivery1Comp4Target.getId(), userGeorgeYoung);
        ///////////////////////////
        // bookmark credentials
        ///////////////////////////
        bookmarkCredential(events, credential3Delivery1.getId(), userGeorgeYoung);
        bookmarkCredential(events, credential4Delivery1.getId(), userGeorgeYoung);
        ///////////////////////////
        // create activity based competency, give privilege, enroll, complete
        ///////////////////////////

        Competence1 comp1 = createCompetence(events,
                    userNickPowell,
                    "Social Network Analysis",
                    "Define social network analysis and its main analysis methods.",
                    "centrality measures, data collection, modularity analysis, network centrality, network structure, social network analysis",
                    0,
                    0,
                    LearningPathType.ACTIVITY);

        Activity1 act1 = createActivity(
                events,
                userNickPowell,
                "Introduction to Social Network Analysis",
                "Introduction into social network analysis for week 3 of DALMOOC by Dragan Gasevic.",
                "https://www.youtube.com/watch?v=2uibqSdHSag",
                ActivityType.VIDEO,
                comp1.getId(),
                0,
                5,
                org.prosolo.services.nodes.data.ActivityResultType.TEXT,
                "Slides",
                "https://www.slideshare.net/dgasevic/introduction-into-social-network-analysis/");

        Activity1 act2 = createActivity(
                events,
                userNickPowell,
                "Example dataset",
                null,
                "<p>Download the example dataset used in the videos and familiarize with the data.</p>",
                ActivityType.TEXT,
                comp1.getId(),
                0,
                3,
                org.prosolo.services.nodes.data.ActivityResultType.TEXT,
                "Example datasets used in the videos",
                "https://s3.amazonaws.com/prosoloedx2/files/3f86bdfd0e8357f7c60c36b38c8fc2c0/Example%20datasets%20used%20in%20the%20videos.pdf");

        Activity1 act3 = createActivity(
                events,
                userNickPowell,
                "Network Modularity and Community Identification",
                "Dragan Gasevic discusses network modularity and community identification in social network analysis for week 3 of DALMOOC. The presentation describes the notion of network modularity as a method used",
                "https://www.slideshare.net/dgasevic/network-modularity-and-community-identification",
                ActivityType.SLIDESHARE,
                comp1.getId(),
                0,
                6,
                org.prosolo.services.nodes.data.ActivityResultType.TEXT,
                "Slides",
                "http://www.slideshare.net/dgasevic/network-modularity-and-community-identification/1");
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class)
                        .publishCompetenceIfNotPublished(comp1.getId(), createUserContext(userNickPowell)));
        //TODO I commmented out the lines below because we changed the rule and user can't learn competency outside credential
        //TargetCompetence1 independentComp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(comp1.getId(), userGeorgeYoung.getId(), createUserContext(userGeorgeYoung)));
//        List<ActivityData> independentComp1TargetActivities = ServiceLocator.getInstance().getService(Activity1Manager.class).getTargetActivitiesData(independentComp1Target.getId());
//        completeActivity(events, independentComp1Target.getId(), independentComp1TargetActivities.get(0).getActivityId(), userGeorgeYoung);

        Competence1 comp2 = createCompetence(events,
                    userNickPowell,
                    "Basic of Prediction Modeling",
                    "Conduct prediction modeling effectively and appropriately",
                    "academic performance, creative potential, social network analysis",
                    0,
                    0,
                    LearningPathType.EVIDENCE);
        givePrivilegeToUsersForCompetency(events, comp2.getId(), UserGroupPrivilege.Learn, userNickPowell, List.of(userGeorgeYoung));
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class)
                .publishCompetenceIfNotPublished(comp2.getId(), createUserContext(userNickPowell)));
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
