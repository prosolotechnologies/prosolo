package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.assessment.CompetenceAssessment;
import org.prosolo.common.domainmodel.credential.LearningEvidence;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.config.AssessmentLoadConfig;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.AssessmentDiscussionMessageData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.UserData;

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

		///////////////////////////
		// assign students to instructor
		///////////////////////////
		// explicitly set Phil Armstrong as an instructor of Helen Campbell
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userHelenCampbell, credential1Delivery1);
		// explicitly set Phil Armstrong as an instructor of Richard Anderson
		assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userRichardAnderson, credential1Delivery1);

		////////////////////////////
		// enroll in competencies
		////////////////////////////
		List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// add pieces of evidence to the all competencies
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


		//////////////////////////////////
		// Create Status wall posts
		//////////////////////////////////
		SocialActivity1 socialActivity1 = createSocialActivity(events, userLoriAbner, "Market analysis and future prospects of Online Education market.", "https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22");
		SocialActivity1 socialActivity2 = createSocialActivity(events, userHelenCampbell, "", "https://www.teachermagazine.com.au/articles/numeracy-is-everyones-business");

//		CommentData comment2 = createNewComment(events, userGeorgeYoung,
//				"Social network analysis has emerged as a key technique in modern sociology.",
//				socialActivity2.getId(),
//				CommentedResourceType.SocialActivity,
//				null);

		//////////////////////////////////////
		// Complete competencies
		//////////////////////////////////////
		markCompetencyAsCompleted(events, credential1Comp1Target.getId(), standard1Competencies.get(0).getCompetenceId(), credential1Delivery1.getId(), userHelenCampbell);

		//////////////////////////////////////
		// Tutor Assessments
		//////////////////////////////////////

		// fetch instructor assessment
		AssessmentManager assessmentService = ServiceLocator.getInstance().getService(AssessmentManager.class);

		Long tutorPhillArmstronfAssessmentHelenCampbellId = assessmentService.getActiveInstructorCredentialAssessmentId(credential1Delivery1.getId(), userHelenCampbell.getId()).get();
		AssessmentDataFull tutorPhillArmstronfAssessmentHelenCampbell = assessmentService.getFullAssessmentData(
				tutorPhillArmstronfAssessmentHelenCampbellId,
				userPhilArmstrong.getId(),
				AssessmentLoadConfig.of(true, true, true));

		CompetenceAssessmentDataFull comp1AssessmentData = tutorPhillArmstronfAssessmentHelenCampbell.getCompetenceAssessmentData().get(0);

		AssessmentDiscussionMessageData newComment = assessmentService.addCommentToCompetenceAssessmentDiscussion(
				comp1AssessmentData.getCompetenceAssessmentId(),
				userPhilArmstrong.getId(),
				"More evidence needed for this focus area",
				createUserContext(userPhilArmstrong));

		/////////////////////////////////////
		// Create one peer assessment
		/////////////////////////////////////
		CompetenceAssessment comp1AssessmentHelenCampbellPeerRichardAnderson = extractResultAndAddEvents(events, assessmentService.requestCompetenceAssessmentAndGetEvents(
				credential1Delivery1.getId(),
				comp1AssessmentData.getCompetenceId(),
				userHelenCampbell.getId(),
				userRichardAnderson.getId(),
				0,
				createUserContext(userHelenCampbell)));
        //accept assessment request
		extractResultAndAddEvents(events, assessmentService.acceptCompetenceAssessmentRequestAndGetEvents(
				comp1AssessmentHelenCampbellPeerRichardAnderson.getId(),
				UserContextData.ofActor(userRichardAnderson.getId())));
		// set grade
		gradeCompetenceAssessmentWithRubric(events,
				comp1AssessmentHelenCampbellPeerRichardAnderson.getId(),
				AssessmentType.PEER_ASSESSMENT,
				userRichardAnderson, rubricData.getLevels().get(3).getId());

		// approve competency
		approveCompetenceAssessment(events, comp1AssessmentHelenCampbellPeerRichardAnderson.getId(),
				credential1Delivery1.getId(),
				comp1AssessmentData.getCompetenceId(),
				userRichardAnderson);

//		///////////////////////////////////////////
//		// Grade and approve instructor assessment
//		///////////////////////////////////////////
//		long credential1Delivery1HelenCampbellInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
//				.getInstructorCredentialAssessmentId(credential1Delivery1.getId(), userHelenCampbell.getId()).get();
//
//		AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1HelenCampbellInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
//
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(0), userPhilArmstrong, 5);
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(1), userPhilArmstrong, 3);
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(2), userPhilArmstrong, 4);
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(3), userPhilArmstrong, 5);
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(4), userPhilArmstrong, 5);
//		gradeCompetenceAssessmentByRubric(events, instructorCredentialAssessmentData.getCompetenceAssessmentData().get(5), userPhilArmstrong, 4);
//		gradeCredentialAssessmentByRubric(events, instructorCredentialAssessmentData, userPhilArmstrong, 3);
//		approveCredentialAssessment(events, instructorCredentialAssessmentData.getCredAssessmentId(), userPhilArmstrong);

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
