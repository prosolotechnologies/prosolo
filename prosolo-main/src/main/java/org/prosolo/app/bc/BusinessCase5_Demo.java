package org.prosolo.app.bc;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;

import java.util.Arrays;
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
		enrollToDelivery(events, organization, credential1Delivery1, userHelenCampbell);
		enrollToDelivery(events, organization, credential1Delivery1, userRichardAnderson);
		enrollToDelivery(events, organization, credential1Delivery1, userStevenTurner);
		enrollToDelivery(events, organization, credential1Delivery1, userJosephGarcia);
		enrollToDelivery(events, organization, credential1Delivery1, userTimothyRivera);
		enrollToDelivery(events, organization, credential1Delivery1, userKevinHall);
		///////////////////////////
		// assign students to instructor
		///////////////////////////
		// explicitly set Phil Armstrong as an instructor of Helen Campbell
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
				credential1Delivery1InstructorPhilArmstrong.getId(), credential1Delivery1.getId(), Arrays.asList(userHelenCampbell.getId()), null, createUserContext(userNickPowell)));
		// explicitly set Phil Armstrong as an instructor of Richard Anderson
		extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(CredentialInstructorManager.class).updateStudentsAssignedToInstructor(
				credential1Delivery1InstructorPhilArmstrong.getId(), credential1Delivery1.getId(), Arrays.asList(userRichardAnderson.getId()), null, createUserContext(userNickPowell)));
		////////////////////////////
		// enroll in competencies
		////////////////////////////
		List<CompetenceData1> standard1Competencies = ServiceLocator.getInstance().getService(Competence1Manager.class).getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
		// we need a reference to the TargetCompetence1
		TargetCompetence1 credential1Comp1Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(0).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp2Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(1).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp3Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(2).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp4Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(3).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp5Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(4).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));
		TargetCompetence1 credential1Comp6Target = extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(Competence1Manager.class).enrollInCompetenceAndGetEvents(standard1Competencies.get(5).getCompetenceId(), userHelenCampbell.getId(), createUserContext(userHelenCampbell)));

		// add pieces of evidence to the all competencies
		addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.LINK,
				"Learning Plan",
				"Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
				"https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
				"learning plan, teaching strategies",
				"Learning plan incorporating teaching strategies.",
				credential1Comp1Target.getId(),
				userHelenCampbell);

		addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.LINK,
				"Teaching Strategies Success Analysis",
				"Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
				"http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
				"teaching strategies",
				"Teaching strategies success analysis for the K-12 programme.",
				credential1Comp1Target.getId(),
				userHelenCampbell);

		LearningEvidenceData evidenceData3 = addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.FILE,
				"New version of the Mathematics teaching program",
				"A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
				PDF_TEST_FILE,
				"teaching program",
				"Contains structure of the new version of a teaching program.",
				credential1Comp2Target.getId(),
				userHelenCampbell);

		addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.FILE,
				"Recording of meeting with supervisor",
				"Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
				MOV_TEST_FILE,
				"meeting logs",
				"Contains feedback on the new version of the teaching program.",
				credential1Comp2Target.getId(),
				userHelenCampbell);

		attachExistingEvidenceToCompetence(evidenceData3.getId(), credential1Comp3Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

		LearningEvidenceData evidenceData5 = addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.FILE,
				"Lesson notes from English language course",
				"Lesson notes from the English language course given on 21 April, 2018.",
				PPT_TEST_FILE,
				"lesson notes, english language",
				"Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.",
				credential1Comp4Target.getId(),
				userHelenCampbell);

		addNewEvidenceAndAttachToCompetence(
				events,
				LearningEvidenceType.FILE,
				"Audio recording of student feedback",
				"Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
						"the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
				MP3_TEST_FILE,
				"student conference",
				"Student feedback on teaching activities to meet the specific learning strengths and needs.",
				credential1Comp5Target.getId(),
				userHelenCampbell);

		attachExistingEvidenceToCompetence(evidenceData5.getId(), credential1Comp6Target.getId(), "Lesson observation notes that record how the teaching strategies designed and implemented by\n" +
				"the teacher have been adjusted to support the learning needs of individual students with disability.");
		//////////////////////////////////
		// Create Status wall posts
		//////////////////////////////////
		createSocialActivity(events, userLoriAbner, "Market analysis and future prospects of Online Education market.", "https://www.marketwatch.com/press-release/online-education-market-2018-top-key-players-k12-inc-pearson-white-hat-managemen-georg-von-holtzbrinck-gmbh-co-2018-08-22");
		createSocialActivity(events, userHelenCampbell, "", "https://www.teachermagazine.com.au/articles/numeracy-is-everyones-business");
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
