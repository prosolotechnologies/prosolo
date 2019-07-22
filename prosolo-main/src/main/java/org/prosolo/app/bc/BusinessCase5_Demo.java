package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.common.data.SelectableData;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.user.StudentProfileManager;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.profile.*;
import org.prosolo.services.user.data.profile.factory.CredentialProfileOptionsFullToBasicFunction;

import java.util.List;

/**
 * @author Nikola Milikic
 * @date 2018-04-25
 * @since 1.2
 */
public class BusinessCase5_Demo extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase5_Demo.class.getName());

	@Override
	protected void createAdditionalDataBC5(EventQueue events) throws Exception {

		////////////////////////////////////////////////////
		// Add 30 tokens to each user from the organization
		////////////////////////////////////////////////////
		ServiceLocator.getInstance().getService(OrganizationManager.class).addTokensToAllOrganizationUsersAndGetEvents(organization.getId(), 30, createUserContext(userNickPowell));

        enableTokensPlugin(30, 2, 2);

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
		// Add credential categories
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

		enrollToDelivery(events, credential2Delivery1, userHelenCampbell);
		enrollToDelivery(events, credential2Delivery1, userRichardAnderson);
		enrollToDelivery(events, credential2Delivery1, userKevinHall);

		enrollToDelivery(events, credential3Delivery1, userHelenCampbell);
		enrollToDelivery(events, credential3Delivery1, userKevinHall);

		///////////////////////////
		// assign students to instructor
		///////////////////////////
		// explicitly set Phil Armstrong as an instructor of Helen Campbell
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userHelenCampbell, credential1Delivery1);
		assignInstructorToStudent(events, credential2Delivery1InstructorPhilArmstrong, userHelenCampbell, credential2Delivery1);
		assignInstructorToStudent(events, credential3Delivery1InstructorPhilArmstrong, userHelenCampbell, credential3Delivery1);
		// explicitly set Phil Armstrong as an instructor of Richard Anderson
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userRichardAnderson, credential1Delivery1);

		////////////////////////////
		// enroll in competencies
		////////////////////////////

		// Standard 1
		List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		List<CompetenceData1> standard1CompetenciesStevenTurner = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(0).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp2TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(1).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));
		TargetCompetence1 credential1Comp3TargetStevenTurner = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(2).getCompetenceId(), userStevenTurner.getId(), createUserContext(userStevenTurner)));

		List<CompetenceData1> standard1CompetenciesTimothyRivera = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userTimothyRivera.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1TargetTimothyRivera = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesTimothyRivera.get(0).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));
		TargetCompetence1 credential1Comp2TargetTimothyRivera = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesTimothyRivera.get(1).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));
		TargetCompetence1 credential1Comp3TargetTimothyRivera = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1CompetenciesTimothyRivera.get(2).getCompetenceId(), userTimothyRivera.getId(), createUserContext(userTimothyRivera)));

		// Standard 2
		List<CompetenceData1> standard2Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential2Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential2Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential2Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential2Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential2Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential2Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential2Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), standard2Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// Standard 3
		List<CompetenceData1> standard3Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential3Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential3Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential3Comp7Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), standard3Competencies.get(6).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		////////////////////////////////////////////////
		// Add pieces of evidence to the competencies
        ////////////////////////////////////////////////
        LearningEvidence evidence1 = createEvidence(
				events,
				LearningEvidenceType.LINK,
				"Learning Plan",
				"Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
				PDF_TEST_FILE,
				"learning plan, teaching strategies",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence1.getId(), credential1Comp1Target.getId(), "Learning plan incorporating teaching strategies.");


		LearningEvidence evidence2 = createEvidence(
				events,
				LearningEvidenceType.LINK,
				"Teaching Strategies Success Analysis",
				"Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
				"http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
				"teaching strategies",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence2.getId(), credential1Comp1Target.getId(), "Teaching strategies success analysis for the K-12 programme.");
		attachExistingEvidenceToCompetence(evidence2.getId(), credential3Comp3Target.getId(), "Demonstrates the user of a range of teaching strategies.");


		LearningEvidence evidence3 = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"New version of the Mathematics teaching program",
				"A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
				PDF1_TEST_FILE,
				"teaching program",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence3.getId(), credential1Comp2Target.getId(), "Contains structure of the new version of a teaching program.");
		attachExistingEvidenceToCompetence(evidence3.getId(), credential1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
		attachExistingEvidenceToCompetence(evidence3.getId(), credential2Comp1Target.getId(), "Includes the content of the new course, as well as the learning strategies.");
		attachExistingEvidenceToCompetence(evidence3.getId(), credential2Comp2Target.getId(), "Demonstrates content organization and teaching sequence.");
		attachExistingEvidenceToCompetence(evidence3.getId(), credential2Comp6Target.getId(), "Several classes cover the basics of the ICT basics.");


		LearningEvidence evidence4 = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Recording of meeting with supervisor",
				"Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
				MOV_TEST_FILE,
				"meeting logs",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence4.getId(), credential1Comp2Target.getId(), "Contains feedback on the new version of the teaching program.");


		LearningEvidence evidence5 = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Lesson notes from English language course",
				"Lesson notes from the English language course given on 21 April, 2018.",
				PPT_TEST_FILE,
				"lesson notes, english language",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence5.getId(), credential1Comp4Target.getId(), "Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.");
		attachExistingEvidenceToCompetence(evidence5.getId(), credential1Comp6Target.getId(), "Lesson observation notes that record how the teaching strategies designed and implemented by\n" +
				"the teacher have been adjusted to support the learning needs of individual students with disability.");
		attachExistingEvidenceToCompetence(evidence5.getId(), credential2Comp3Target.getId(), "Explains the assessment and reporting process.");
		attachExistingEvidenceToCompetence(evidence5.getId(), credential2Comp5Target.getId(), "Demonstrates my understanding of literacy and numeracy teaching strategies.");


		LearningEvidence evidence6 = createEvidence(
				events,
				LearningEvidenceType.FILE,
				"Audio recording of student feedback",
				"Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
						"the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
				MP3_TEST_FILE,
				"student conference",
				userHelenCampbell);
		attachExistingEvidenceToCompetence(evidence6.getId(), credential1Comp5Target.getId(), "Student feedback on teaching activities to meet the specific learning strengths and needs.");
		attachExistingEvidenceToCompetence(evidence6.getId(), credential1Comp4Target.getId(), "Includes the feedback of a student with Aboriginal background about the newly develop course in relation to their culture and language.");
		attachExistingEvidenceToCompetence(evidence6.getId(), credential3Comp5Target.getId(), "Demonstrates the use of several verbal and non-verbal communication strategies in order to support student engagement.");
		attachExistingEvidenceToCompetence(evidence6.getId(), credential3Comp7Target.getId(), "Contains comments of parents about the course.");

        LearningEvidence evidence1StevenTurner = createEvidence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                PDF_TEST_FILE,
                "learning plan, teaching strategies",
                userStevenTurner);
        attachExistingEvidenceToCompetence(evidence1.getId(), credential1Comp1TargetStevenTurner.getId(), "Learning plan incorporating teaching strategies.");

        LearningEvidence evidence1TimothyRivera = createEvidence(
                events,
                LearningEvidenceType.FILE,
                "Recording of meeting with supervisor",
                "Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
                MOV_TEST_FILE,
                "meeting logs",
                userTimothyRivera);
        attachExistingEvidenceToCompetence(evidence4.getId(), credential1Comp2TargetTimothyRivera.getId(), "Contains feedback on the new version of the teaching program.");

        /////////////////////////////////////////////
        // complete competencies from the Standard 2
        /////////////////////////////////////////////
        markCompetencyAsCompleted(events, credential1Comp1TargetStevenTurner.getId(), standard1CompetenciesStevenTurner.get(0).getCompetenceId(), credential1Delivery1.getId(), userStevenTurner);
        markCompetencyAsCompleted(events, credential1Comp2TargetTimothyRivera.getId(), standard1CompetenciesTimothyRivera.get(1).getCompetenceId(), credential1Delivery1.getId(), userTimothyRivera);


        /////////////////////////////////////////////
		// complete competencies from the Standard 2
		/////////////////////////////////////////////
		markCompetencyAsCompleted(events, credential2Comp1Target.getId(), standard2Competencies.get(0).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential2Comp2Target.getId(), standard2Competencies.get(1).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential2Comp3Target.getId(), standard2Competencies.get(2).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential2Comp4Target.getId(), standard2Competencies.get(3).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential2Comp5Target.getId(), standard2Competencies.get(4).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential2Comp6Target.getId(), standard2Competencies.get(5).getCompetenceId(), credential2Delivery1.getId(), userHelenCampbell);

		/////////////////////////////////////////////
		// complete competencies from the Standard 3
		/////////////////////////////////////////////
		markCompetencyAsCompleted(events, credential3Comp1Target.getId(), standard3Competencies.get(0).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp2Target.getId(), standard3Competencies.get(1).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp3Target.getId(), standard3Competencies.get(2).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp4Target.getId(), standard3Competencies.get(3).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp5Target.getId(), standard3Competencies.get(4).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp6Target.getId(), standard3Competencies.get(5).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);
		markCompetencyAsCompleted(events, credential3Comp6Target.getId(), standard3Competencies.get(6).getCompetenceId(), credential3Delivery1.getId(), userHelenCampbell);

		///////////////////////////////////////////////
		// Ask for competence assessment - Standard 2
		///////////////////////////////////////////////
        CompetenceAssessmentData peerAssessmentCred1Comp1ByHelenCampbell = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), standard1CompetenciesStevenTurner.get(0).getCompetenceId(), userStevenTurner, userHelenCampbell.getId(), 2);
        updateCompetenceBlindAssessmentMode(events, standard1CompetenciesStevenTurner.get(0).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerAssessmentCred1Comp1ByHelenCampbell.getAssessmentId(),
                UserContextData.ofActor(userHelenCampbell.getId())));

        CompetenceAssessmentData peerAssessmentCred1Comp2ByHelenCampbell = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), standard1CompetenciesTimothyRivera.get(1).getCompetenceId(), userTimothyRivera, userHelenCampbell.getId(), 2);
        updateCompetenceBlindAssessmentMode(events, standard1CompetenciesTimothyRivera.get(1).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerAssessmentCred1Comp2ByHelenCampbell.getAssessmentId(),
                UserContextData.ofActor(userHelenCampbell.getId())));

		///////////////////////////////////////////////
		// Ask for competence assessment - Standard 2
		///////////////////////////////////////////////

		CompetenceAssessmentData peerAssessmentCred2Comp1ByRichardAnderson = askPeerForCompetenceAssessment(events, credential2Delivery1.getId(), standard2Competencies.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard2Competencies.get(0).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred2Comp1ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred2Comp2ByRichardAnderson = askPeerForCompetenceAssessment(events, credential2Delivery1.getId(), standard2Competencies.get(1).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard2Competencies.get(1).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred2Comp2ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred2Comp3ByRichardAnderson = askPeerForCompetenceAssessment(events, credential2Delivery1.getId(), standard2Competencies.get(2).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard2Competencies.get(2).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred2Comp3ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred2Comp1ByKevinHall = askPeerForCompetenceAssessment(events, credential2Delivery1.getId(), standard2Competencies.get(0).getCompetenceId(), userHelenCampbell, userKevinHall.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard2Competencies.get(0).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred2Comp1ByKevinHall.getAssessmentId(),
				UserContextData.ofActor(userKevinHall.getId())));

		///////////////////////////////////////////////
		// Ask for competence assessment - Standard 3
		///////////////////////////////////////////////

		CompetenceAssessmentData peerAssessmentCred3Comp1ByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3Competencies.get(0).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard3Competencies.get(0).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp1ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp2ByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3Competencies.get(1).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard3Competencies.get(1).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp2ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp3ByRichardAnderson = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3Competencies.get(2).getCompetenceId(), userHelenCampbell, userRichardAnderson.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard3Competencies.get(2).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp3ByRichardAnderson.getAssessmentId(),
				UserContextData.ofActor(userRichardAnderson.getId())));

		CompetenceAssessmentData peerAssessmentCred3Comp1ByKevinHall = askPeerForCompetenceAssessment(events, credential3Delivery1.getId(), standard3Competencies.get(0).getCompetenceId(), userHelenCampbell, userKevinHall.getId(), 2);
		updateCompetenceBlindAssessmentMode(events, standard3Competencies.get(0).getCompetenceId(), BlindAssessmentMode.BLIND, userNickPowell);
		//accept assessment request
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
				peerAssessmentCred3Comp1ByKevinHall.getAssessmentId(),
				UserContextData.ofActor(userKevinHall.getId())));


		//////////////////////////////////////////////
		// Grade and approve assessment - Standard 2
		//////////////////////////////////////////////
		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred2Comp1ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(3).getId());
		approveCompetenceAssessment(events, peerAssessmentCred2Comp1ByRichardAnderson.getAssessmentId(), credential2Delivery1.getId(),  standard2Competencies.get(0).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred2Comp2ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred2Comp2ByRichardAnderson.getAssessmentId(), credential2Delivery1.getId(),  standard2Competencies.get(1).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred2Comp3ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred2Comp3ByRichardAnderson.getAssessmentId(), credential2Delivery1.getId(),  standard2Competencies.get(2).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred2Comp1ByKevinHall.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userKevinHall, rubricData.getLevels().get(1).getId());
		approveCompetenceAssessment(events, peerAssessmentCred2Comp1ByKevinHall.getAssessmentId(), credential2Delivery1.getId(),  standard2Competencies.get(0).getCompetenceId(), userKevinHall);

		//////////////////////////////////////////////
		// Grade and approve assessment - Standard 3
		//////////////////////////////////////////////
		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp1ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp1ByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3Competencies.get(0).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp2ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(3).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp2ByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3Competencies.get(1).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp3ByRichardAnderson.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(2).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp3ByRichardAnderson.getAssessmentId(), credential3Delivery1.getId(),  standard3Competencies.get(2).getCompetenceId(), userRichardAnderson);

		gradeCompetenceAssessmentWithRubric(events, peerAssessmentCred3Comp1ByKevinHall.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userKevinHall, rubricData.getLevels().get(1).getId());
		approveCompetenceAssessment(events, peerAssessmentCred3Comp1ByKevinHall.getAssessmentId(), credential3Delivery1.getId(),  standard3Competencies.get(0).getCompetenceId(), userKevinHall);

		// Instructor assessment - Standard 2
		long credential2Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getActiveInstructorCredentialAssessmentId(credential2Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull instructorCredential2AssessmentData = getCredentialAssessmentData(credential2Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, instructorCredential2AssessmentData, userPhilArmstrong, AssessmentType.INSTRUCTOR_ASSESSMENT, rubricData.getLevels().get(0).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredential2AssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp1Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp2Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp3Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp4Target.getId()) {
				lvl = rubricData.getLevels().get(1).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp5Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp6Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			}
			gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
		}
		approveCredentialAssessment(events,
				instructorCredential2AssessmentData.getCredAssessmentId(),
				"Helen Campbell has shown the mastery in the area of content creation and has a refined teaching skills to complement it.",
				instructorCredential2AssessmentData.getCredentialId(), userPhilArmstrong);

		// Instructor assessment - Standard 3
		long credential3Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getActiveInstructorCredentialAssessmentId(credential3Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull instructorCredential3AssessmentData = getCredentialAssessmentData(credential3Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, instructorCredential3AssessmentData, userPhilArmstrong, AssessmentType.INSTRUCTOR_ASSESSMENT, rubricData.getLevels().get(1).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredential3AssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp1Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp2Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp3Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp4Target.getId()) {
				lvl = rubricData.getLevels().get(1).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp5Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp6Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential3Comp7Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			}
			gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
		}
		approveCredentialAssessment(events,
				instructorCredential3AssessmentData.getCredAssessmentId(),
				"Helen Campbell demonstrated an admirable level of knowledge in planning and implementation of effective teaching and learning.",
				instructorCredential3AssessmentData.getCredentialId(), userPhilArmstrong);

		// Self-assessment - Standard 2
		long credential1Delivery1HelenCampbellSelfAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
				.getSelfCredentialAssessmentId(credential2Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull selfCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1HelenCampbellSelfAssessmentId, userHelenCampbell.getId(), AssessmentType.SELF_ASSESSMENT);
		gradeCredentialAssessmentWithRubric(events, selfCredentialAssessmentData, userHelenCampbell, AssessmentType.SELF_ASSESSMENT, rubricData.getLevels().get(1).getId());
		for (CompetenceAssessmentDataFull competenceAssessmentData : selfCredentialAssessmentData.getCompetenceAssessmentData()) {
			long lvl = 0;
			if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp1Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp2Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp3Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp4Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp5Target.getId()) {
				lvl = rubricData.getLevels().get(3).getId();
			} else if (competenceAssessmentData.getTargetCompetenceId() == credential2Comp6Target.getId()) {
				lvl = rubricData.getLevels().get(2).getId();
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
		createSocialActivity(events, userLoriAbner, "Market analysis and future prospects of Online Education market.", "https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22");
		createSocialActivity(events, userHelenCampbell, "", "https://www.teachermagazine.com.au/articles/numeracy-is-everyones-business");

		///////////////////////////////////////////////////////////
		// Add the Standard 2 to the Helen Campbell's profile page
		///////////////////////////////////////////////////////////
		long credential2Delivery1HelenCampbellTargetCredentialId = ServiceLocator.getInstance().getService(CredentialManager.class).getTargetCredentialId(credential2Delivery1.getId(), userHelenCampbell.getId());

		ServiceLocator.getInstance().getService(StudentProfileManager.class).addCredentialsToProfile(userHelenCampbell.getId(), List.of(credential2Delivery1HelenCampbellTargetCredentialId));

		CredentialProfileOptionsData credential2ProfileHelenCampbellForEdit = ServiceLocator.getInstance().getService(StudentProfileManager.class).getCredentialProfileOptions(credential2Delivery1HelenCampbellTargetCredentialId);

		for (AssessmentByTypeProfileOptionsData assessmentData : credential2ProfileHelenCampbellForEdit.getAssessments()) {
			for (SelectableData<AssessmentProfileData> selectableAssessment : assessmentData.getAssessments()) {
				selectableAssessment.setSelected(true);
			}
		}

		for (CompetenceProfileOptionsData competency : credential2ProfileHelenCampbellForEdit.getCompetences()) {
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
				ServiceLocator.getInstance().getService(CredentialProfileOptionsFullToBasicFunction.class).apply(credential2ProfileHelenCampbellForEdit));
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
