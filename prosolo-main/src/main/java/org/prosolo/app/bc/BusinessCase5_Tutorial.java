package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.organization.settings.EvidenceRepositoryPlugin;
import org.prosolo.common.domainmodel.organization.settings.AssessmentsPlugin;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.assessments.AssessmentNotificationData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.organization.AssessmentTokensPluginData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.EvidenceRepositoryPluginData;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.services.user.data.profile.factory.CredentialProfileOptionsFullToBasicFunction;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2018-04-25
 * @since 1.2
 */
public class BusinessCase5_Tutorial extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase5_Tutorial.class.getName());

	@Override
	protected void createAdditionalDataBC5(EventQueue events) throws Exception {

		///////////////////////////////////
		// Set Rachel Wiggins as Admin
		///////////////////////////////////
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserManager.class).updateUserAndGetEvents(
				userRachelWiggins.getId(),
				userRachelWiggins.getName(),
				userRachelWiggins.getLastname(),
				userRachelWiggins.getEmail(),
				true,
				false,
				userRachelWiggins.getPassword(),
				"Administrator",
				20,
				List.of(roleAdmin.getId()),
				List.of(),
				createUserContext(userNickPowell)));

		///////////////////////////////////
		// Add Manager role to more users
		///////////////////////////////////
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserManager.class).updateUserAndGetEvents(
				userSonyaElston.getId(),
				userSonyaElston.getName(),
				userSonyaElston.getLastname(),
				userSonyaElston.getEmail(),
				true,
				false,
				userSonyaElston.getPassword(),
				"Assistant Professor",
				20,
				List.of(roleManager.getId()),
				List.of(),
				createUserContext(userNickPowell)));

		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserManager.class).updateUserAndGetEvents(
				userTaniaCortese.getId(),
				userTaniaCortese.getName(),
				userTaniaCortese.getLastname(),
				userTaniaCortese.getEmail(),
				true,
				false,
				userTaniaCortese.getPassword(),
				"Associate Professor",
				20,
				List.of(roleAdmin.getId()),
				List.of(),
				createUserContext(userNickPowell)));


		////////////////////////////////////////////////////
		// Add 30 tokens to each user from the organization
		////////////////////////////////////////////////////
		ServiceLocator.getInstance().getService(OrganizationManager.class).addTokensToAllOrganizationUsersAndGetEvents(organization.getId(), 30, createUserContext(userNickPowell));

		enableAssessmentTokens(30, 2, 2);




		//////////////////////////////////////////////////////
		// Set several student to be available for assessment
		//////////////////////////////////////////////////////
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userHelenCampbell.getId(), true);
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userRichardAnderson.getId(), true);
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userStevenTurner.getId(), true);
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userJosephGarcia.getId(), true);
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userTimothyRivera.getId(), true);
		ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKevinHall.getId(), true);

		///////////////////////////////////////////////////////////////////////////////////
		// Disable keywords in Evidence Repository plugin and disable text-based evidence
		///////////////////////////////////////////////////////////////////////////////////
        EvidenceRepositoryPlugin evidenceRepositoryPlugin = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationPlugin(EvidenceRepositoryPlugin.class, organization.getId());
        EvidenceRepositoryPluginData evidenceRepositoryPluginData = new EvidenceRepositoryPluginData(evidenceRepositoryPlugin);
        evidenceRepositoryPluginData.setKeywordsEnabled(false);
        evidenceRepositoryPluginData.setTextEvidenceEnabled(false);
		ServiceLocator.getInstance().getService(OrganizationManager.class).updateEvidenceRepositoryPlugin(organization.getId(), evidenceRepositoryPluginData);

		///////////////////////////////////////////
		// Disable comments in Assessments plugin
		///////////////////////////////////////////
        AssessmentsPlugin assessmentsPlugin = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationPlugin(AssessmentsPlugin.class, organization.getId());
        AssessmentTokensPluginData assessmentsPluginData = new AssessmentTokensPluginData(assessmentsPlugin);
        assessmentsPluginData.setPrivateDiscussionEnabled(false);
		ServiceLocator.getInstance().getService(OrganizationManager.class).updateAssessmentTokensPlugin(assessmentsPluginData);

		enableAssessmentTokens(30, 2, 2);

        //////////////////////////////////////////////////
        // Set few student to be available for assessment
        //////////////////////////////////////////////////
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userHelenCampbell.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userRichardAnderson.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userStevenTurner.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userJosephGarcia.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userTimothyRivera.getId(), true);
        ServiceLocator.getInstance().getService(UserManager.class).updateAssessmentAvailability(userKevinHall.getId(), true);

        ////////////////////////////////
		// Add credential categoriesCompetenceViewBeanManager
		////////////////////////////////
		createCredentialCategories(events, "Professional Knowledge");
		createCredentialCategories(events, "Professional Practice");
		createCredentialCategories(events, "Professional Engagement");
		CredentialCategoryData category1 = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationCredentialCategoriesData(organization.getId()).get(1);
		CredentialCategoryData category2 = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationCredentialCategoriesData(organization.getId()).get(2);
		CredentialCategoryData category3 = ServiceLocator.getInstance().getService(OrganizationManager.class).getOrganizationCredentialCategoriesData(organization.getId()).get(0);
		assignCategoryToCredential(events, credential1.getId(), category1, userNickPowell);
		assignCategoryToCredential(events, credential2.getId(), category1, userNickPowell);
		assignCategoryToCredential(events, credential3.getId(), category2, userNickPowell);
		assignCategoryToCredential(events, credential4.getId(), category2, userNickPowell);
		assignCategoryToCredential(events, credential5.getId(), category2, userNickPowell);
		assignCategoryToCredential(events, credential6.getId(), category3, userNickPowell);
		assignCategoryToCredential(events, credential7.getId(), category3, userNickPowell);

		////////////////////////////////
		// Update deliveries
		////////////////////////////////
		CredentialData credential1Delivery1Data = ServiceLocator.getInstance().getService(CredentialManager.class).getCredentialDataForEdit(credential1Delivery1.getId());
		credential1Delivery1Data.setAssessorAssignment(CredentialData.AssessorAssignmentMethodData.BY_STUDENTS);
		credential1Delivery1 = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(ResourceFactory.class).updateCredential(credential1Delivery1Data, createUserContext(userNickPowell)));

		////////////////////////////////
		// Add follow relations
		////////////////////////////////
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userLoriAbner.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userSamanthaDell.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userRachelWiggins.getId(), createUserContext(userPaulEdwards)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userTimothyRivera)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userKevinMitchell) ));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userGeorgeYoung)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userPaulEdwards.getId(), createUserContext(userRachelWiggins)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userStevenTurner.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userJosephGarcia.getId(), createUserContext(userHelenCampbell)));
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(FollowResourceManager.class).followUserAndGetEvents(userLoriAbner.getId(), createUserContext(userHelenCampbell)));

		///////////////////////////
		// enroll users to delivery
		///////////////////////////
		enrollToDelivery(events, credential1Delivery1, userHelenCampbell);
		enrollToDelivery(events, credential1Delivery1, userRichardAnderson);
		enrollToDelivery(events, credential1Delivery1, userStevenTurner);
		enrollToDelivery(events, credential1Delivery1, userJosephGarcia);
		enrollToDelivery(events, credential1Delivery1, userTimothyRivera);
		enrollToDelivery(events, credential1Delivery1, userKevinHall);

		enrollToDelivery(events, credential3Delivery1, userHelenCampbell);
		enrollToDelivery(events, credential3Delivery1, userRichardAnderson);
		enrollToDelivery(events, credential3Delivery1, userKevinHall);
		enrollToDelivery(events, credential3Delivery1, userStevenTurner);

		enrollToDelivery(events, credential4Delivery1, userHelenCampbell);
		enrollToDelivery(events, credential4Delivery1, userKevinHall);

		///////////////////////////
		// assign students to instructor
		///////////////////////////
		// explicitly set Phil Armstrong as an instructor of Helen Campbell
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userHelenCampbell, credential1Delivery1);
		assignInstructorToStudent(events, credential3Delivery1InstructorPhilArmstrong, userHelenCampbell, credential3Delivery1);
		assignInstructorToStudent(events, credential4Delivery1InstructorPhilArmstrong, userHelenCampbell, credential4Delivery1);
		// explicitly set Phil Armstrong as an instructor of Steven Turner
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userStevenTurner, credential1Delivery1);
		// explicitly set Phil Armstrong as an instructor of Richard Anderson
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userRichardAnderson, credential1Delivery1);

		////////////////////////////
		// enroll in competencies
		////////////////////////////

		// Standard 1
		// Helen Campbell
		List<CompetenceData1> standard1CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp2TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp3TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp4TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp5TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp6TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// Richard Anderson
		List<CompetenceData1> standard1CompetenciesRichardAnderson = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userRichardAnderson.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(0).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
		TargetCompetence1 credential1Comp2TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(1).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
		TargetCompetence1 credential1Comp3TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(2).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
		TargetCompetence1 credential1Comp4TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(3).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
		TargetCompetence1 credential1Comp5TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(4).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));
		TargetCompetence1 credential1Comp6TargetRichardAnderson = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesRichardAnderson.get(5).getCompetenceId(), userRichardAnderson.getId(), createUserContext(userRichardAnderson)));

		// Steven Turner
		List<CompetenceData1> standard1CompetenciesStevenTurner = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(0).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp2TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(1).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp3TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(2).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp4TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(3).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp5TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(4).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp6TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(5).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));


		// Standard 3
        // Helen Campbell
		List<CompetenceData1> standard3CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential3Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential3Comp1TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp2TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp3TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp4TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp5TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp6TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp7TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(6).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// Kevin Hall
		List<CompetenceData1> standard3CompetenciesKevinHall = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential3Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential3Comp1TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp2TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp3TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp4TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp5TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp6TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
		TargetCompetence1 credential3Comp7TargetKevinHall = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(6).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

		// Steven Turner
		List<CompetenceData1> standard3CompetenciesStevenTurner = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential3Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential3Comp1TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(0).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp2TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(1).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp3TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(2).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp4TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(3).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp5TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(4).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp6TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(5).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential3Comp7TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(6).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));

		// Standard 4
		List<CompetenceData1> standard4CompetenciesHelenCampbell = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential4Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential4Comp1TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential4Comp2TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential4Comp3TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential4Comp4TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential4Comp5TargetHelenCampbell = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// add pieces of evidence to the all competencies
		LearningEvidence evidence1HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.LINK,
				"Learning Plan",
				"Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
				PDF_TEST_FILE,
				"learning plan, teaching strategies",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence1HelenCampbell.getId(), credential1Comp1TargetHelenCampbell.getId(), "Learning plan incorporating teaching strategies.");
		attachExistingEvidenceToCompetence(evidence1HelenCampbell.getId(), credential3Comp1TargetHelenCampbell.getId(), "Learning plan incorporating my learning goals.");


		LearningEvidence evidence2HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.LINK,
				"Teaching Strategies Success Analysis",
				"Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
				"http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
				"teaching strategies",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence2HelenCampbell.getId(), credential1Comp1TargetHelenCampbell.getId(), "Teaching strategies success analysis for the K-12 programme.");
		attachExistingEvidenceToCompetence(evidence2HelenCampbell.getId(), credential3Comp3TargetHelenCampbell.getId(), "Demonstrates the user of a range of teaching strategies.");


		LearningEvidence evidence3HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"New version of the Mathematics teaching program",
				"A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
				PDF1_TEST_FILE,
				"teaching program",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence3HelenCampbell.getId(), credential1Comp2TargetHelenCampbell.getId(), "Contains structure of the new version of a teaching program.");
		attachExistingEvidenceToCompetence(evidence3HelenCampbell.getId(), credential1Comp3TargetHelenCampbell.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
		attachExistingEvidenceToCompetence(evidence3HelenCampbell.getId(), credential4Comp1TargetHelenCampbell.getId(), "Involves activities that foster student participation.");
		attachExistingEvidenceToCompetence(evidence3HelenCampbell.getId(), credential4Comp2TargetHelenCampbell.getId(), "Demonstrates my management of classrom activities");
		attachExistingEvidenceToCompetence(evidence3HelenCampbell.getId(), credential3Comp2TargetHelenCampbell.getId(), "Contains Mathematics course plan and structure");


		LearningEvidence evidence4HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Recording of meeting with supervisor",
				"Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
				MOV_TEST_FILE,
				"meeting logs",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence4HelenCampbell.getId(), credential1Comp2TargetHelenCampbell.getId(), "Contains feedback on the new version of the teaching program.");
		attachExistingEvidenceToCompetence(evidence4HelenCampbell.getId(), credential3Comp1TargetHelenCampbell.getId(), "My supervisor's comments on my learning goals.");


		LearningEvidence evidence5HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Lesson notes from English language course",
				"Lesson notes from the English language course given on 21 April, 2018.",
				PPT_TEST_FILE,
				"lesson notes, english language",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence5HelenCampbell.getId(), credential1Comp4TargetHelenCampbell.getId(), "Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.");
		attachExistingEvidenceToCompetence(evidence5HelenCampbell.getId(), credential1Comp6TargetHelenCampbell.getId(), "Lesson observation notes that record how the teaching strategies designed and implemented by\n" +
				"the teacher have been adjusted to support the learning needs of individual students with disability.");
		attachExistingEvidenceToCompetence(evidence5HelenCampbell.getId(), credential4Comp3TargetHelenCampbell.getId(), "Demonstrates my use of practical approaches to manage challenging behaviour.");
		attachExistingEvidenceToCompetence(evidence5HelenCampbell.getId(), credential4Comp4TargetHelenCampbell.getId(), "Contains notes about taking care of student safety.");


		LearningEvidence evidence6HelenCampbell = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Audio recording of student feedback",
				"Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
						"the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
				MP3_TEST_FILE,
				"student conference",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential1Comp5TargetHelenCampbell.getId(), "Student feedback on teaching activities to meet the specific learning strengths and needs.");
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential1Comp4TargetHelenCampbell.getId(), "Includes the feedback of a student with Aboriginal background about the newly develop course in relation to their culture and language.");
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential3Comp5TargetHelenCampbell.getId(), "Demonstrates the use of several verbal and non-verbal communication strategies in order to support student engagement.");
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential3Comp7TargetHelenCampbell.getId(), "Contains comments of parents about the course.");

		LearningEvidence evidence1KevinHall = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Audio recording of student feedback",
				"Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
						"the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
				MP3_TEST_FILE,
				"student conference",
				userKevinHall);
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential3Comp5TargetKevinHall.getId(), "Demonstrates the use of several verbal and non-verbal communication strategies in order to support student engagement.");
		attachExistingEvidenceToCompetence(evidence6HelenCampbell.getId(), credential3Comp7TargetKevinHall.getId(), "Contains comments of parents about the course.");


		/////////////////////////////////////////////
		// add evidence summary for the Standard 1
		/////////////////////////////////////////////
        ServiceLocator.getInstance().getService(Competence1Manager.class).saveEvidenceSummary(credential1Comp1TargetHelenCampbell.getId(), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam luctus, mauris vel consequat molestie, felis felis malesuada mauris, a pellentesque augue tellus ac sapien. Fusce iaculis id risus eu iaculis. Vivamus eu lacus metus. Sed vehicula dignissim quam, a eleifend justo viverra sit amet. Fusce in feugiat leo. Morbi vitae dapibus lacus. Fusce a tortor vestibulum, commodo metus eget, dictum magna.");

		/////////////////////////////////////////////
		// complete competencies from the Standard 1
		/////////////////////////////////////////////
		markCompetencyAsCompleted(events, credential1Comp1TargetHelenCampbell.getId(), standard1CompetenciesHelenCampbell.get(0).getCompetenceId(), credential1Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential1Comp3TargetHelenCampbell.getId(), standard1CompetenciesHelenCampbell.get(2).getCompetenceId(), credential1Delivery1.getId(), userHelenCampbell);

		/////////////////////////////////////////////
		// complete competencies from the Standard 3
		/////////////////////////////////////////////
		markCompetencyAsCompleted(events, credential3Comp1TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp2TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(1).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp3TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(2).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp4TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(3).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp5TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(4).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp6TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(5).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp6TargetHelenCampbell.getId(), standard3CompetenciesHelenCampbell.get(6).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);

        markCompetencyAsCompleted(events, credential3Comp5TargetKevinHall.getId(), standard3CompetenciesKevinHall.get(4).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);

        markCompetencyAsCompleted(events, credential3Comp5TargetStevenTurner.getId(), standard3CompetenciesStevenTurner.get(4).getCompetenceId(), credential3Delivery1.getId(), userStevenTurner);

        /////////////////////////////////////////////
		// complete competencies from the Standard 4
		/////////////////////////////////////////////
		markCompetencyAsCompleted(events, credential4Comp1TargetHelenCampbell.getId(), standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), credential4Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential4Comp2TargetHelenCampbell.getId(), standard4CompetenciesHelenCampbell.get(1).getCompetenceId(), credential4Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential4Comp3TargetHelenCampbell.getId(), standard4CompetenciesHelenCampbell.get(2).getCompetenceId(), credential4Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential4Comp4TargetHelenCampbell.getId(), standard4CompetenciesHelenCampbell.get(3).getCompetenceId(), credential4Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential4Comp5TargetHelenCampbell.getId(), standard4CompetenciesHelenCampbell.get(4).getCompetenceId(), credential4Delivery1.getId(), userHelenCampbell);


		////////////////////////////////////////////////////////////////////////////////
		// Enable double blind assessment mode for competencies in Standards 1, 3, and 4
		////////////////////////////////////////////////////////////////////////////////
		for (CompetenceData1 competenceData : standard1CompetenciesHelenCampbell) {
			updateCompetenceBlindAssessmentMode(events, competenceData.getCompetenceId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);
		}
		for (CompetenceData1 competenceData : standard3CompetenciesHelenCampbell) {
			updateCompetenceBlindAssessmentMode(events, competenceData.getCompetenceId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);
		}
		for (CompetenceData1 competenceData : standard4CompetenciesHelenCampbell) {
			updateCompetenceBlindAssessmentMode(events, competenceData.getCompetenceId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);
		}

		///////////////////////////////////////////////
		// Ask for assessment - Standard 1
		///////////////////////////////////////////////

		CompetenceAssessmentData peerAssessmentCred1Comp1ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), standard1CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);

		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred1Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		// notify assessor to assess credential - Helen Campbell
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).notifyAssessorToAssessCredentialAndGetEvents(
                AssessmentNotificationData.of(
                        credential1Delivery1.getId(),
                        userPhilArmstrong.getId(),
                        userHelenCampbell.getId(),
                        AssessmentType.INSTRUCTOR_ASSESSMENT),
                createUserContext(userHelenCampbell)));

        // notify assessor to assess competence 2 - Steven Turner
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).notifyAssessorToAssessCompetenceAndGetEvents(
                AssessmentNotificationData.of(
                        credential1Delivery1.getId(),
                        standard1CompetenciesStevenTurner.get(1).getCompetenceId(),
                        userPhilArmstrong.getId(),
                        userStevenTurner.getId(),
                        AssessmentType.INSTRUCTOR_ASSESSMENT),
                createUserContext(userStevenTurner)));

		///////////////////////////////////////////////
		// Ask for assessment - Standard 3
		///////////////////////////////////////////////

        // Helen Campbell
		CompetenceAssessmentData peerAssessmentCred3Comp1ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp2ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp3ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp1ForHelenCampbellByKevinHall = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userKevinHall.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp1ForHelenCampbellByKevinHall.getAssessmentId(),
				UserContextData.ofActor(userKevinHall.getId())));

        // Kevin Hall
        CompetenceAssessmentData peerAssessmentCred3Comp5ForKevinHallByHelenCampbell = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesKevinHall.get(4).getCompetenceId(), userKevinHall, userHelenCampbell.getId(), 2);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerAssessmentCred3Comp5ForKevinHallByHelenCampbell.getAssessmentId(),
                UserContextData.ofActor(userHelenCampbell.getId())));

        // Steven Turner Hall
        CompetenceAssessmentData peerAssessmentCred3Comp5ForStevenTurnerByHelenCampbell = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3CompetenciesStevenTurner.get(4).getCompetenceId(), userStevenTurner, userHelenCampbell.getId(), 2);

		///////////////////////////////////////////////
		// Ask for assessment - Standard 4
		///////////////////////////////////////////////

		CompetenceAssessmentData peerAssessmentCred4Comp1ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred4Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred4Comp2ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(1).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred4Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred4Comp3ForHelenCampbellByRichardAnderson = askPeerForCompetenceAssessment(events, credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(2).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred4Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred4Comp1ForHelenCampbellByKevinHall = askPeerForCompetenceAssessment(events, credential4Delivery1.getId(), standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), userHelenCampbell, userKevinHall.getId(), 2);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred4Comp1ForHelenCampbellByKevinHall.getAssessmentId(),
				UserContextData.ofActor(userKevinHall.getId())));


		//////////////////////////////////////////////
		// Grade and approve assessment - Standard 1
		//////////////////////////////////////////////
		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred1Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred1Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), credential1Delivery1.getId(),  standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userRichardAnderson);

		//////////////////////////////////////////////
		// Grade and approve assessment - Standard 3
		//////////////////////////////////////////////
        // Helen Campbell
		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(3).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3CompetenciesHelenCampbell.get(1).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3CompetenciesHelenCampbell.get(2).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp1ForHelenCampbellByKevinHall.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userKevinHall, rubricData.getLevels().get(1).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp1ForHelenCampbellByKevinHall.getAssessmentId(), credential3Delivery1.getId(),  standard3CompetenciesHelenCampbell.get(0).getCompetenceId(), userKevinHall);

		// Kevin Hall
        gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp5ForKevinHallByHelenCampbell.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userHelenCampbell, rubricData.getLevels().get(2).getId());


        //////////////////////////////////////////////
		// Grade and approve assessment - Standard 2
		//////////////////////////////////////////////
		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred4Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(3).getId());
		approveCompetenceAssessment(events, peerAssessmentCred4Comp1ForHelenCampbellByRichardAnderson.getAssessmentId(), credential4Delivery1.getId(),  standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred4Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred4Comp2ForHelenCampbellByRichardAnderson.getAssessmentId(), credential4Delivery1.getId(),  standard4CompetenciesHelenCampbell.get(1).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred4Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred4Comp3ForHelenCampbellByRichardAnderson.getAssessmentId(), credential4Delivery1.getId(),  standard4CompetenciesHelenCampbell.get(2).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred4Comp1ForHelenCampbellByKevinHall.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userKevinHall, rubricData.getLevels().get(1).getId());
		approveCompetenceAssessment(events, peerAssessmentCred4Comp1ForHelenCampbellByKevinHall.getAssessmentId(), credential4Delivery1.getId(),  standard4CompetenciesHelenCampbell.get(0).getCompetenceId(), userKevinHall);


		// Instructor assessment - Standard 1
		long credential1Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class).
				getActiveInstructorCredentialAssessmentId(credential1Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull tutorPhillArmstronfAssessmentHelenCampbell = getCredentialAssessmentData(credential1Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);

		// add comment to assessment
		AssessmentDiscussionMessageData newComment = ServiceLocator.getInstance().getService(AssessmentManager.class).addCommentToCompetenceAssessmentDiscussion(
                tutorPhillArmstronfAssessmentHelenCampbell.getCompetenceAssessmentData().get(0).getCompetenceAssessmentId(),
				userPhilArmstrong.getId(),
				"More evidence needed for this focus area",
				createUserContext(userPhilArmstrong));

		// Instructor assessment - Standard 3
		long credential3Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getActiveInstructorCredentialAssessmentId(credential3Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull instructorCredential3AssessmentData = getCredentialAssessmentData(credential3Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, instructorCredential3AssessmentData, userPhilArmstrong, AssessmentType.INSTRUCTOR_ASSESSMENT, rubricData.getLevels().get(1).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredential3AssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp1TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp2TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp3TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp4TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(1).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp5TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp6TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp7TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			}
			gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
		}
		approveCredentialAssessment(events,
				instructorCredential3AssessmentData.getCredAssessmentId(),
				"Helen Campbell demonstrated an admirable level of knowledge in planning and implementation of effective teaching and learning.",
				instructorCredential3AssessmentData.getCredentialId(), userPhilArmstrong);

		// Instructor assessment - Standard 4
		long credential4Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getActiveInstructorCredentialAssessmentId(credential4Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull instructorCredential4AssessmentData = getCredentialAssessmentData(credential4Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, instructorCredential4AssessmentData, userPhilArmstrong, AssessmentType.INSTRUCTOR_ASSESSMENT, rubricData.getLevels().get(0).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredential4AssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp1TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp2TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp3TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp4TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(1).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp5TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			}
			gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
		}
		approveCredentialAssessment(events,
				instructorCredential4AssessmentData.getCredAssessmentId(),
				"Helen Campbell has shown the mastery in the area of creating and maintaining supportive and safe learning environments.",
				instructorCredential4AssessmentData.getCredentialId(), userPhilArmstrong);

		// Self-assessment - Standard 4
		long credential4Delivery1HelenCampbellSelfAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getSelfCredentialAssessmentId(credential4Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull selfCredentialAssessmentData = getCredentialAssessmentData(credential4Delivery1HelenCampbellSelfAssessmentId, userHelenCampbell.getId(), AssessmentType.SELF_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, selfCredentialAssessmentData, userHelenCampbell, AssessmentType.SELF_ASSESSMENT, rubricData.getLevels().get(1).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : selfCredentialAssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp1TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp2TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp3TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp4TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential4Comp5TargetHelenCampbell.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			}
			gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userHelenCampbell, lvl);
		}
		approveCredentialAssessment(events,
				selfCredentialAssessmentData.getCredAssessmentId(),
				"I believe I have gained the skills to create an appropriate teaching content for students of different cultural backgrounds and to align the my teaching strategies to their needs.",
				selfCredentialAssessmentData.getCredentialId(), userHelenCampbell);

		//////////////////////////////////
		// Create Status wall posts
		//////////////////////////////////
		SocialActivity1 socialActivity1 = createSocialActivity(events, userLoriAbner, "Market analysis and future prospects of Online Education market.", "https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22");
		SocialActivity1 socialActivity2 = createSocialActivity(events, userHelenCampbell, "", "https://www.teachermagazine.com.au/articles/lifting-student-self-esteem-and-achievement");

//		CommentData comment2 = createNewComment(events, userGeorgeYoung,
//				"Social network analysis has emerged as a key technique in modern sociology.",
//				socialActivity2.getId(),
//				CommentedResourceType.SocialActivity,
//				null);

		///////////////////////////////////////////////////////////
		// Add the Standard 4 to the Helen Campbell's profile page
		///////////////////////////////////////////////////////////
		long credential4Delivery1HelenCampbellTargetCredentialId = ServiceLocator.getInstance().getService(CredentialManager.class).getTargetCredentialId(credential4Delivery1.getId(), userHelenCampbell.getId());

		ServiceLocator.getInstance().getService(StudentProfileManager.class).addCredentialsToProfile(userHelenCampbell.getId(), List.of(credential4Delivery1HelenCampbellTargetCredentialId));

		CredentialProfileOptionsData credential4ProfileHelenCampbellForEdit = ServiceLocator.getInstance().getService(StudentProfileManager.class).getCredentialProfileOptions(credential4Delivery1HelenCampbellTargetCredentialId);

		for (AssessmentByTypeProfileOptionsData assessmentData : credential4ProfileHelenCampbellForEdit.getAssessments()) {
			for (SelectableData<AssessmentProfileData> selectableAssessment : assessmentData.getAssessments()) {
				selectableAssessment.setSelected(true);
			}
		}

		for (CompetenceProfileOptionsData competency : credential4ProfileHelenCampbellForEdit.getCompetences()) {
			for (AssessmentByTypeProfileOptionsData competencyAssessmentData : competency.getAssessments()) {
				for (SelectableData<AssessmentProfileData> selectableAssessment : competencyAssessmentData.getAssessments()) {
					selectableAssessment.setSelected(true);
				}
			}

			for (SelectableData<CompetenceEvidenceProfileData> evidenceSelectableData : competency.getEvidence()) {
				evidenceSelectableData.setSelected(true);
			}
		}
		ServiceLocator.getInstance().getService(StudentProfileManager.class).updateCredentialProfileOptions(
				ServiceLocator.getInstance().getService(CredentialProfileOptionsFullToBasicFunction.class).apply(credential4ProfileHelenCampbellForEdit));

		/////////////////////////////
		// Update Personal Settings
		/////////////////////////////
		UserData userDataHelenCampbell = ServiceLocator.getInstance().getService(UserManager.class).getUserData(userHelenCampbell.getId());
		userDataHelenCampbell.setLocationName("Adelaide SA, Australia");
		userDataHelenCampbell.setLatitude(-34.92849890000001);
		userDataHelenCampbell.setLongitude(138.60074559999998);
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(UserManager.class).saveAccountChangesAndGetEvents(userDataHelenCampbell, createUserContext(userHelenCampbell)));

		ServiceLocator.getInstance().getService(SocialNetworksManager.class).createSocialNetworkAccount(
				SocialNetworkName.LINKEDIN,
				"http://www.linkedin.com/in/helen.campbell-022b0a98",
				createUserContext(userHelenCampbell));

		ServiceLocator.getInstance().getService(SocialNetworksManager.class).createSocialNetworkAccount(
				SocialNetworkName.TWITTER,
				"https://twitter.com/HelenCampbell212",
				createUserContext(userHelenCampbell));

		ServiceLocator.getInstance().getService(SocialNetworksManager.class).createSocialNetworkAccount(
				SocialNetworkName.FACEBOOK,
				"https://www.facebook.com/HelenCampbell212",
				createUserContext(userHelenCampbell));

		ServiceLocator.getInstance().getService(SocialNetworksManager.class).createSocialNetworkAccount(
				SocialNetworkName.BLOG,
				"http://helencampbell.blogger.com",
				createUserContext(userHelenCampbell));
	}

	@Override
	protected String getBusinessCaseInitLog() {
		return "Initializing business case 5 - UniSA data";
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
