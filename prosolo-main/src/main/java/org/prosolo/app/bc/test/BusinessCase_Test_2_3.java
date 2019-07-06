package org.prosolo.app.bc.test;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.BaseBusinessCase5;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.BlindAssessmentMode;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.event.EventQueue;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.data.AssessmentDataFull;
import org.prosolo.services.assessment.data.CompetenceAssessmentData;
import org.prosolo.services.assessment.data.CompetenceAssessmentDataFull;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;

import java.util.List;

/**
 * @author stefanvuckovic
 * @date 2019-01-16
 * @since 1.2.0
 */
public class BusinessCase_Test_2_3 extends BaseBusinessCase5 {

    private static Logger logger = Logger.getLogger(BusinessCase_Test_2_3.class.getName());

    @Override
    protected void createAdditionalDataBC5(EventQueue events) throws Exception {
        Competence1Manager compManager = ServiceLocator.getInstance().getService(Competence1Manager.class);
        ///////////////////////////
        // create credential category and set it for credentials
        ///////////////////////////
        createCredentialCategories(events, "Category 1");
        CredentialCategoryData category = ServiceLocator.getInstance().getService(OrganizationManager.class)
                .getOrganizationCredentialCategoriesData(organization.getId()).get(0);
        assignCategoryToCredential(events, credential2.getId(), category, userNickPowell);
        assignCategoryToCredential(events, credential3.getId(), category, userNickPowell);
        ///////////////////////////
        // enroll users to deliveries
        ///////////////////////////
        enrollToDelivery(events, credential1Delivery1, userRichardAnderson);
        enrollToDelivery(events, credential1Delivery1, userHelenCampbell);
        enrollToDelivery(events, credential1Delivery1, userStevenTurner);
        enrollToDelivery(events, credential1Delivery1, userKevinHall);
        enrollToDelivery(events, credential2Delivery1, userKevinHall);
        enrollToDelivery(events, credential3Delivery1, userKevinHall);
        enrollToDelivery(events, credential4Delivery1, userKevinHall);
        ////////////////////////////
        // assign students to instructor
        ////////////////////////////
        assignInstructorToStudent(events, credential1Delivery1InstructorPhilArmstrong, userKevinHall, credential1Delivery1);
        ////////////////////////////
        // enroll in competencies
        ////////////////////////////
        //enroll steven turner
        List<CompetenceData1> credential1Delivery1CompetencesSteven = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userStevenTurner.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        enrollToCompetencies(events, credential1Delivery1.getId(), credential1Delivery1CompetencesSteven, userStevenTurner);

        //enroll helen campbell
        List<CompetenceData1> credential1Delivery1CompetencesHelen = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userHelenCampbell.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        enrollToCompetencies(events, credential1Delivery1.getId(), credential1Delivery1CompetencesHelen, userHelenCampbell);
        //enroll kevin hall
        List<CompetenceData1> credential1Delivery1Competences = compManager.getCompetencesForCredential(credential1Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential1Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential1Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential1Delivery1.getId(), credential1Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        List<CompetenceData1> credential2Delivery1Competences = compManager.getCompetencesForCredential(credential2Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential2Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential2Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential2Delivery1.getId(), credential2Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        List<CompetenceData1> credential3Delivery1Competences = compManager.getCompetencesForCredential(credential3Delivery1.getId(), userKevinHall.getId(), new CompetenceLoadConfig.CompetenceLoadConfigBuilder().create());
        TargetCompetence1 credential3Delivery1Comp1Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(0).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp2Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(1).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp3Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(2).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp4Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(3).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp5Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(4).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp6Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(5).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));
        TargetCompetence1 credential3Delivery1Comp7Target = extractResultAndAddEvents(events, compManager.enrollInCompetenceAndGetEvents(credential3Delivery1.getId(), credential3Delivery1Competences.get(6).getCompetenceId(), userKevinHall.getId(), createUserContext(userKevinHall)));

        ///////////////////////////
        // add evidence to competencies
        ///////////////////////////
        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        LearningEvidenceData evidenceData3 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential2Delivery1Comp1Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Recording of meeting with supervisor",
                "Recording of the meeting with my supervisor Rick Sanchez from 15 June, 2018.",
                MOV_TEST_FILE,
                "meeting logs",
                "Contains feedback on the new version of the teaching program.",
                credential2Delivery1Comp2Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential2Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        LearningEvidenceData evidenceData5 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Lesson notes from English language course",
                "Lesson notes from the English language course given on 21 April, 2018.",
                PPT_TEST_FILE,
                "lesson notes, english language",
                "Lesson observation notes and discussion about effective teaching strategies that have been modified to reflect the learning needs and histories of Aboriginal and Torres Strait Islander students.",
                credential2Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential2Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential2Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential2Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "Audio recording of student feedback",
                "Recording of student-led conference outcomes informing the development of teaching activities and strategies to meet\n" +
                        "the specific learning strengths and needs of students across a full range of abilities. 01 May, 2018.",
                MP3_TEST_FILE,
                "student conference",
                "Student feedback on teaching activities to meet the specific learning strengths and needs.",
                credential3Delivery1Comp1Target.getId(),
                userKevinHall);

        LearningEvidenceData evidenceData7 = addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program 2",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential3Delivery1Comp2Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential3Delivery1Comp2Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 2",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential3Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential3Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential3Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential3Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential3Delivery1Comp7Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Learning Plan 2",
                "Learning plan incorporating teaching strategies that have been selected specifically to address the students’ physical, social or intellectual development and characteristics",
                "https://s3.amazonaws.com/prosolo.nikola/files/6efd5a265b12209a9d88cea9c79aaa6c/Learnign%20Plan.pdf",
                "learning plan, teaching strategies",
                "Learning plan incorporating teaching strategies.",
                credential1Delivery1Comp1Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential1Delivery1Comp1Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.LINK,
                "Teaching Strategies Success Analysis 3",
                "Analysis of the success of teaching strategies selected on the progress of the student, and how their learning has improved",
                "http://hellen.myblongspot.com/analysis-of-the-success-of-teaching-strategies/",
                "teaching strategies",
                "Teaching strategies success analysis for the K-12 programme.",
                credential1Delivery1Comp2Target.getId(),
                userKevinHall);

        addNewEvidenceAndAttachToCompetence(
                events,
                LearningEvidenceType.FILE,
                "New version of the Mathematics teaching program 3",
                "A new version of the teaching program for the Mathematics course created based on the advice from the supervisor.",
                PDF_TEST_FILE,
                "teaching program",
                "Contains structure of the new version of a teaching program.",
                credential1Delivery1Comp3Target.getId(),
                userKevinHall);

        attachExistingEvidenceToCompetence(evidenceData3.getId(), credential1Delivery1Comp4Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData5.getId(), credential1Delivery1Comp5Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        attachExistingEvidenceToCompetence(evidenceData7.getId(), credential1Delivery1Comp6Target.getId(), "Includes teaching strategies that have been designed and implemented based on the identified learning strengths and needs of students from diverse linguistic backgrounds.");
        //////////////////////////////////////
        // complete competencies
        //////////////////////////////////////
        markCompetencyAsCompleted(events, credential1Delivery1Comp1Target.getId(), credential1Delivery1Competences.get(0).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential1Delivery1Comp2Target.getId(), credential1Delivery1Competences.get(1).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential1Delivery1Comp3Target.getId(), credential1Delivery1Competences.get(2).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential1Delivery1Comp4Target.getId(), credential1Delivery1Competences.get(3).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential1Delivery1Comp5Target.getId(), credential1Delivery1Competences.get(4).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential1Delivery1Comp6Target.getId(), credential1Delivery1Competences.get(5).getCompetenceId(), credential1Delivery1.getId(), userKevinHall);

        markCompetencyAsCompleted(events, credential2Delivery1Comp1Target.getId(), credential2Delivery1Competences.get(0).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential2Delivery1Comp2Target.getId(), credential2Delivery1Competences.get(1).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential2Delivery1Comp3Target.getId(), credential2Delivery1Competences.get(2).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential2Delivery1Comp4Target.getId(), credential2Delivery1Competences.get(3).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential2Delivery1Comp5Target.getId(), credential2Delivery1Competences.get(4).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential2Delivery1Comp6Target.getId(), credential2Delivery1Competences.get(5).getCompetenceId(), credential2Delivery1.getId(), userKevinHall);

        markCompetencyAsCompleted(events, credential3Delivery1Comp1Target.getId(), credential3Delivery1Competences.get(0).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp2Target.getId(), credential3Delivery1Competences.get(1).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp3Target.getId(), credential3Delivery1Competences.get(2).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp4Target.getId(), credential3Delivery1Competences.get(3).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp5Target.getId(), credential3Delivery1Competences.get(4).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp6Target.getId(), credential3Delivery1Competences.get(5).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);
        markCompetencyAsCompleted(events, credential3Delivery1Comp7Target.getId(), credential3Delivery1Competences.get(6).getCompetenceId(), credential3Delivery1.getId(), userKevinHall);

        ////////////////////////////////
        // ask for competence assessment
        ////////////////////////////////
        CompetenceAssessmentData peerComp2AssessmentByHelen = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), credential1Delivery1Comp2Target.getCompetence().getId(), userKevinHall, userHelenCampbell.getId(), 0);
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp2Target.getCompetence().getId(), BlindAssessmentMode.BLIND, userNickPowell);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerComp2AssessmentByHelen.getAssessmentId(),
                UserContextData.ofActor(userHelenCampbell.getId())));
        CompetenceAssessmentData peerComp2AssessmentByRichard = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), credential1Delivery1Comp2Target.getCompetence().getId(), userKevinHall, userRichardAnderson.getId(), 0);
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp2Target.getCompetence().getId(), BlindAssessmentMode.OFF, userNickPowell);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerComp2AssessmentByRichard.getAssessmentId(),
                UserContextData.ofActor(userRichardAnderson.getId())));

        CompetenceAssessmentData peerComp4AssessmentByRichard = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userRichardAnderson.getId(), 0);
        //accept assessment request
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerComp4AssessmentByRichard.getAssessmentId(),
                UserContextData.ofActor(userRichardAnderson.getId())));
        CompetenceAssessmentData peerComp4AssessmentBySteven = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userStevenTurner.getId(), 0);
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp4Target.getCompetence().getId(), BlindAssessmentMode.DOUBLE_BLIND, userNickPowell);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerComp4AssessmentBySteven.getAssessmentId(),
                UserContextData.ofActor(userStevenTurner.getId())));
        CompetenceAssessmentData peerComp4AssessmentByHelen = askPeerForCompetenceAssessment(events, credential1Delivery1.getId(), credential1Delivery1Comp4Target.getCompetence().getId(), userKevinHall, userHelenCampbell.getId(), 0);
        updateCompetenceBlindAssessmentMode(events, credential1Delivery1Comp4Target.getCompetence().getId(), BlindAssessmentMode.OFF, userNickPowell);
        extractResultAndAddEvents(events, ServiceLocator.getInstance().getService(AssessmentManager.class).acceptCompetenceAssessmentRequestAndGetEvents(
                peerComp4AssessmentByHelen.getAssessmentId(),
                UserContextData.ofActor(userHelenCampbell.getId())));
        ////////////////////////////////
        // grade and approve assessment
        ////////////////////////////////
        gradeCompetenceAssessmentWithRubric(events, peerComp2AssessmentByHelen.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userHelenCampbell, rubricData.getLevels().get(2).getId());
        approveCompetenceAssessment(events, peerComp2AssessmentByHelen.getAssessmentId(), credential1Delivery1.getId(), credential1Delivery1Comp2Target.getCompetence().getId(), userHelenCampbell);
        gradeCompetenceAssessmentWithRubric(events, peerComp2AssessmentByRichard.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(3).getId());
        approveCompetenceAssessment(events, peerComp2AssessmentByRichard.getAssessmentId(), credential1Delivery1.getId(), credential1Delivery1Comp2Target.getCompetence().getId(), userRichardAnderson);

        gradeCompetenceAssessmentWithRubric(events, peerComp4AssessmentByRichard.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userRichardAnderson, rubricData.getLevels().get(1).getId());
        approveCompetenceAssessment(events, peerComp4AssessmentByRichard.getAssessmentId(), credential1Delivery1.getId(), credential1Delivery1Comp4Target.getCompetence().getId(), userRichardAnderson);
        gradeCompetenceAssessmentWithRubric(events, peerComp4AssessmentByHelen.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userHelenCampbell, rubricData.getLevels().get(0).getId());
        approveCompetenceAssessment(events, peerComp4AssessmentByHelen.getAssessmentId(), credential1Delivery1.getId(), credential1Delivery1Comp4Target.getCompetence().getId(), userHelenCampbell);
        gradeCompetenceAssessmentWithRubric(events, peerComp4AssessmentBySteven.getAssessmentId(), AssessmentType.PEER_ASSESSMENT, userStevenTurner, rubricData.getLevels().get(2).getId());

        //grade and approve instructor assessment
        long credential1Delivery1KevinHallInstructorAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getActiveInstructorCredentialAssessmentId(credential1Delivery1.getId(), userKevinHall.getId()).get();
        AssessmentDataFull instructorCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1KevinHallInstructorAssessmentId, userPhilArmstrong.getId(), AssessmentType.INSTRUCTOR_ASSESSMENT);
        gradeCredentialAssessmentWithRubric(events, instructorCredentialAssessmentData, userPhilArmstrong, AssessmentType.INSTRUCTOR_ASSESSMENT, rubricData.getLevels().get(2).getId());
        for (CompetenceAssessmentDataFull competenceAssessmentData : instructorCredentialAssessmentData.getCompetenceAssessmentData()) {
            long lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp1Target.getId()) {
                lvl = rubricData.getLevels().get(1).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp2Target.getId()) {
                lvl = rubricData.getLevels().get(3).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp3Target.getId()) {
                lvl = rubricData.getLevels().get(1).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp4Target.getId()) {
                lvl = rubricData.getLevels().get(0).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp5Target.getId()) {
                lvl = rubricData.getLevels().get(3).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp6Target.getId()) {
                lvl = rubricData.getLevels().get(2).getId();
            }
            gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userPhilArmstrong, lvl);
        }
        approveCredentialAssessment(events, instructorCredentialAssessmentData.getCredAssessmentId(), instructorCredentialAssessmentData.getCredentialId(), userPhilArmstrong);

        //grade and approve self assessment
        long credential1Delivery1KevinHallSelfAssessmentId = ServiceLocator.getInstance().getService(AssessmentManager.class)
                .getSelfCredentialAssessmentId(credential1Delivery1.getId(), userKevinHall.getId()).get();
        AssessmentDataFull selfCredentialAssessmentData = getCredentialAssessmentData(credential1Delivery1KevinHallSelfAssessmentId, userKevinHall.getId(), AssessmentType.SELF_ASSESSMENT);
        gradeCredentialAssessmentWithRubric(events, selfCredentialAssessmentData, userKevinHall, AssessmentType.SELF_ASSESSMENT, rubricData.getLevels().get(1).getId());
        for (CompetenceAssessmentDataFull competenceAssessmentData : selfCredentialAssessmentData.getCompetenceAssessmentData()) {
            long lvl = 0;
            if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp1Target.getId()) {
                lvl = rubricData.getLevels().get(0).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp2Target.getId()) {
                lvl = rubricData.getLevels().get(2).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp3Target.getId()) {
                lvl = rubricData.getLevels().get(3).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp4Target.getId()) {
                lvl = rubricData.getLevels().get(1).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp5Target.getId()) {
                lvl = rubricData.getLevels().get(0).getId();
            } else if (competenceAssessmentData.getTargetCompetenceId() == credential1Delivery1Comp6Target.getId()) {
                lvl = rubricData.getLevels().get(1).getId();
            }
            gradeCompetenceAssessmentWithRubric(events, competenceAssessmentData, userKevinHall, lvl);
        }
        approveCredentialAssessment(events, selfCredentialAssessmentData.getCredAssessmentId(), selfCredentialAssessmentData.getCredentialId(), userKevinHall);
    }

    @Override
    protected String getBusinessCaseInitLog() {
        return "Initializing business case - test 2.3";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
